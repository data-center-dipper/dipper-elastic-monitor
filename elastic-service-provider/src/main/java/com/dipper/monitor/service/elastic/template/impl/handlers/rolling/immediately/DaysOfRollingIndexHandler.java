package com.dipper.monitor.service.elastic.template.impl.handlers.rolling.immediately;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 按照天滚动的模版
 * 索引模版 格式 lcc-logs-yyyyMMdd-*
 * 比如 lcc-logs-20230201-*
 * 那么这个对应的索引是 lcc-logs-20230201-0000001
 * 那么滚动一次变成 lcc-logs-20230201-0000002
 */
public class DaysOfRollingIndexHandler extends AbstractRollingIndexByTemplateHandler {

    private static final Logger log = LoggerFactory.getLogger(DaysOfRollingIndexHandler.class);



    // 索引模版 格式 lcc-logs-yyyyMMdd-*
    private String indexPatterns = null;
    // 索引前缀 带时间 lcc-logs-yyyyMMdd
    private String indexPatternsPrefixHaveDate = null;
    // 索引前缀 不带时间 lcc-logs-
    private String indexPatternsPrefixNoDate = null;
    // 索引前缀 不带时间,增加*号 lcc-logs-* 或者 lcc-logs*
    private String indexPatternsPrefixNoDateAddXing = null;

    public DaysOfRollingIndexHandler(EsUnconvertedTemplate esUnconvertedTemplate) {
        super(esUnconvertedTemplate);

        indexPatterns = esUnconvertedTemplate.getIndexPatterns();
        indexPatternsPrefixHaveDate = getIndexPrefixHaveDate();
        indexPatternsPrefixNoDate = getIndexPrefixNoDateAndTail();
        indexPatternsPrefixNoDateAddXing = indexPatternsPrefixNoDate+ "*";
    }

    /**
     * 获取索引前缀不带时间
     * indexPatterns 格式有以下几种样式
     * lcc-logs-yyyyMMdd-*
     * lcc-yyyyMMdd-*
     * lcc-yyyyMMdd
     *
     * 清解析成
     *
     * lcc-logs-
     * lcc--
     * lcc-
     *
     */
    /**
     * 获取索引前缀，去除 yyyyMMdd 时间格式部分，并清理结尾的特殊符号。
     * 支持格式：
     * - lcc-logs-yyyyMMdd-*
     * - lcc-yyyyMMdd-*
     * - lcc-yyyyMMdd
     *
     * @return 清理后的前缀，如 "lcc-logs" 或 "lcc"
     */
    private String getIndexPrefixNoDateAndTail() {
        if (indexPatterns == null || indexPatterns.isEmpty()) {
            log.error("indexPatterns 为空");
            return "";
        }

        int dateIndex = indexPatterns.indexOf("yyyyMMdd");
        if (dateIndex == -1) {
            log.warn("indexPatterns 中未找到 'yyyyMMdd'：{}", indexPatterns);
            return "";
        }

        String prefix = indexPatterns.substring(0, dateIndex);

        // 去除结尾的 -, _, 空格等
        return prefix.replaceAll("[-_\\s]+$", "");
    }

    /**
     * indexPatterns 格式有以下几种样式
     * lcc-logs-yyyyMMdd-*
     * lcc--yyyyMMdd-*
     * lcc--yyyyMMdd
     *
     * 清解析成
     *
     * lcc-logs-yyyyMMdd
     * lcc--yyyyMMdd
     * lcc--yyyyMMdd
     *
     * @return
     */
    private String getIndexPrefixHaveDate() {
        if (indexPatterns == null || indexPatterns.isEmpty()) {
            log.error("indexPatterns 为空");
            return "";
        }

        String prefix = indexPatterns;

        // 去除 -* 或 *
        if (prefix.endsWith("-*")) {
            prefix = prefix.substring(0, prefix.length() - 2);
        } else if (prefix.endsWith("*")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }

        // 去除结尾可能的连续 -
        prefix = prefix.replaceAll("-+$", "");

        // 确保包含 yyyyMMdd
        if (!prefix.contains("yyyyMMdd")) {
            log.warn("indexPatterns 不包含 yyyyMMdd: {}", indexPatterns);
            return "";
        }

        return prefix;
    }

    public void handle() {
        // 1. 循环每个 indexPatterns,这里先是一个吧
        log.info("开始处理索引模式: {}", indexPatterns);
        
        // 2. 删除未来索引
        deleteFeatureTemplate(indexPatterns);

        // 创建模版信息
        createNowTemplate();

        // 滚动索引
        rollIndex(indexPatterns);

        // 创建未来索引
        createFeatureIndex();
    }

    /**
     * 创建未来索引
     * 当前时间比如是 20250515 那么未来索引就是 20250516,20250517,20250518
     * 我们创建未来15天的索引
     * lcc-logs-20250516-0000001
     * lcc-logs-20250517-0000001
     * lcc-logs-20250518-0000001
     * ....
     * 等等
     */
    private void createFeatureIndex() {
    }

    private void rollIndex(String indexPatterns) {
        try {
            // 1. 根据模版获取符合模版的索引列表
            List<String> indices = elasticRealIndexService.listIndexNameByPrefix(indexPatterns);
            
            // 2. 如果没有索引，跳过
            if (indices == null || indices.isEmpty()) {
                log.info("没有找到符合模式 {} 的索引，跳过滚动操作", indexPatterns);
                createFirstIndex();
                return;
            }
            
            // 3. 把所有进行排序，获取第一个，得到当前最新索引 比如索引 aaa-xxx-yyyyMMDD-0000001
            indices.sort((a, b) -> b.compareTo(a)); // 降序排序，最新的索引在前面
            String currentLatestIndex = indices.get(0);
            log.info("当前最新索引: {}", currentLatestIndex);
            
            // 4. 生成新的索引名称 （当前最新索引+1） 比如索引 aaa-xxx-yyyyMMDD-0000002
            String newIndexName = generateNextIndexName(currentLatestIndex);
            log.info("新生成的索引名称: {}", newIndexName);
            
            // 5. 生成最新的别名信息 比如索引 aaa-xxx-yyyyMMDD 指向 aaa-xxx-yyyyMMDD-0000002
            String aliasName = generateAliasName(currentLatestIndex);
            log.info("生成的别名: {}", aliasName);
            
            // 6. 根据别名前缀获取别名
            List<String> aliases = elasticAliasService.listAliasByIndexPatterns(indexPatternsPrefixNoDateAddXing);
            
            // 7. 循环设置别名不可写，并且索引的生命周期结束
            for (String alias : aliases) {
                elasticAliasService.setAliasReadOnly(alias);
                elasticRealLifecyclePoliciesService.lifeCycleEnd(alias);
            }
            
            // 8. 创建新的索引 并且指定别名信息
//            JSONObject templateJson = templatePreviewService.previewEffectTemplate(esUnconvertedTemplate.getId());
//            elasticClientService.createIndex(newIndexName, templateJson);
            elasticAliasService.addAlias(newIndexName, aliasName);
            
            // 9.添加索引可写
            elasticAliasService.changeIndexWrite(newIndexName,aliasName, true);
            
            log.info("索引滚动完成: {} -> {}", currentLatestIndex, newIndexName);
        } catch (Exception e) {
            log.error("滚动索引时发生错误: {}", e.getMessage(), e);
        }
    }

    private String generateNextIndexName(String currentIndexName) {
        // 假设索引格式为 aaa-xxx-yyyyMMDD-0000001
        Pattern pattern = Pattern.compile("(.*-\\d{8})-(\\d+)$");
        Matcher matcher = pattern.matcher(currentIndexName);
        
        if (matcher.find()) {
            String prefix = matcher.group(1);
            int sequence = Integer.parseInt(matcher.group(2));
            return String.format("%s-%07d", prefix, sequence + 1);
        } else {
            // 如果不符合预期格式，添加当前日期和序列号
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(new Date());
            return String.format("%s-%s-%07d", currentIndexName, today, 1);
        }
    }

    private String generateAliasName(String indexName) {
        // 从索引名称中提取别名，去掉最后的序列号部分
        Pattern pattern = Pattern.compile("(.*-\\d{8})-\\d+$");
        Matcher matcher = pattern.matcher(indexName);
        
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            // 如果不符合预期格式，使用索引名称作为别名基础
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(new Date());
            return indexName + "-" + today;
        }
    }

    private void createNowTemplate() {
        try {
            // 1. 获取当前时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(new Date());
            log.info("当前日期: {}", today);
            
            // 2. 生成当前时间模版
            JSONObject templateJson = templatePreviewService.previewEffectTemplate(esUnconvertedTemplate.getId());
            
            // 3. 创建模版信息
            String templateName = esUnconvertedTemplate.getEnName();
            elasticRealTemplateService.saveOrUpdateTemplate(templateName, templateJson);
            
            log.info("模板 {} 创建/更新成功", templateName);
        } catch (Exception e) {
            log.error("创建模板时发生错误: {}", e.getMessage(), e);
        }
    }

    private void deleteFeatureTemplate(String indexPatterns) {
        try {
            // 1. 根据模版获取符合模版的索引列表
            List<String> indices = elasticRealIndexService.listIndexNameByPrefix(indexPatterns);
            
            // 2. 如果没有索引，跳过
            if (indices == null || indices.isEmpty()) {
                log.info("没有找到符合模式 {} 的索引，跳过删除操作", indexPatterns);
                return;
            }
            
            // 3. 获取当前时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(new Date());
            int currentDate = Integer.parseInt(today);
            
            // 4. 循环索引，获取索引的时间 比如索引 aaa-xxx-yyyyMMDD-0000001 获取到 20200301
            for (String indexName : indices) {
                int indexDate = extractDateFromIndex(indexName);
                
                // 5. 如果索引时间大于当前时间，删除索引（未来索引）
                if (indexDate > currentDate) {
                    log.info("删除未来索引: {}, 索引日期: {}, 当前日期: {}", indexName, indexDate, currentDate);
                    elasticClientService.executeDeleteApi(indexName,null);
                }
            }
        } catch (Exception e) {
            log.error("删除未来索引时发生错误: {}", e.getMessage(), e);
        }
    }
    
    private int extractDateFromIndex(String indexName) {
        // 从索引名称中提取日期部分，假设格式为 aaa-xxx-yyyyMMDD-0000001
        Pattern pattern = Pattern.compile(".*-(\\d{8})-\\d+$");
        Matcher matcher = pattern.matcher(indexName);
        
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0; // 如果没有找到日期格式，返回0
    }
    
    /**
     * 创建第一个索引
     * 当没有查询到符合模式的索引时，创建初始索引
     */
    private void createFirstIndex() {
        try {
            // 1. 获取索引模式
            String indexPatterns = esUnconvertedTemplate.getIndexPatterns();

            // 2. 获取当前日期
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String today = sdf.format(new Date());
            log.info("当前日期: {}", today);

            // 3. 提取前缀，去除 yyyyMMdd 和 *
            if(indexPatterns.endsWith("-*")){
                indexPatterns = indexPatterns.substring(0, indexPatterns.length() - 2);
            }else if(indexPatterns.endsWith("*") ) {
                indexPatterns = indexPatterns.substring(0, indexPatterns.length() - 1);
            }
            // ailpha-logs-yyyyMMdd 获取时间之前的前缀信息
            String prefix = indexPatterns.replace("-yyyyMMdd", "");
            prefix = prefix.replace("yyyyMMdd", "");
            log.info("前缀: {}", prefix);

            // 4. 生成第一个索引名称，格式为：ailpha-logs-20250515-0000001
            String firstIndexName = String.format("%s-%s-%07d", prefix, today, 1);
            log.info("生成第一个索引名称: {}", firstIndexName);

            // 5. 生成别名，格式为：ailpha-logs-20250515
            String aliasName = String.format("%s-%s", prefix, today);
            log.info("生成别名: {}", aliasName);

            // 6. 创建索引
            JSONObject templateJson = templatePreviewService.previewEffectTemplate(esUnconvertedTemplate.getId());
            elasticRealIndexService.createIndex(firstIndexName, templateJson);

            // 7. 添加别名
            elasticAliasService.addAlias(firstIndexName, aliasName);

            // 8. 设置别名可写
            elasticAliasService.changeIndexWrite(firstIndexName, aliasName, true);

            log.info("成功创建第一个索引: {}, 别名: {}", firstIndexName, aliasName);
        } catch (Exception e) {
            log.error("创建第一个索引时发生错误: {}", e.getMessage(), e);
        }
    }
}
