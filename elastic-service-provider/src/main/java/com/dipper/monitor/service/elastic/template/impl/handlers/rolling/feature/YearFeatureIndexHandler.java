package com.dipper.monitor.service.elastic.template.impl.handlers.rolling.feature;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.utils.elastic.IndexPatternsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 按年滚动的模版
 * 索引模版 格式 lcc-logs-yyyy-*
 * 比如 lcc-logs-2023-*
 * 那么这个对应的索引是 lcc-logs-2023-0000001
 * 每年创建一个新的索引，如 lcc-logs-2024-0000001
 */
public class YearFeatureIndexHandler extends AbstractFeatureIndexHandler {

    private static final Logger log = LoggerFactory.getLogger(YearFeatureIndexHandler.class);

    // 索引模版 格式 lcc-logs-yyyy-*
    private String indexPatterns = null;
    // 索引前缀 带时间 lcc-logs-yyyy
    private String indexPatternsPrefixHaveDate = null;
    // 索引前缀 不带时间 lcc-logs-
    private String indexPatternsPrefixNoDate = null;
    // 索引前缀 不带时间,增加*号 lcc-logs-* 或者 lcc-logs*
    private String indexPatternsPrefixNoDateAddXing = null;

    public YearFeatureIndexHandler(EsUnconvertedTemplate esUnconvertedTemplate) {
        super(esUnconvertedTemplate);

        indexPatterns = esUnconvertedTemplate.getIndexPatterns();
        indexPatternsPrefixHaveDate =getIndexPrefixHaveDate();
        indexPatternsPrefixNoDate = getIndexPrefixNoDateAndTail();
        indexPatternsPrefixNoDateAddXing = indexPatternsPrefixNoDate+ "*";
    }

    /**
     * 获取索引前缀，去除 yyyy 时间格式部分，并清理结尾的特殊符号。
     * 支持格式：
     * - lcc-logs-yyyy-*
     * - lcc-yyyy-*
     * - lcc-yyyy
     *
     * @return 清理后的前缀，如 "lcc-logs" 或 "lcc"
     */
    private String getIndexPrefixNoDateAndTail() {
        if (indexPatterns == null || indexPatterns.isEmpty()) {
            log.error("indexPatterns 为空");
            return "";
        }

        int dateIndex = indexPatterns.indexOf("yyyy");
        if (dateIndex == -1) {
            log.warn("indexPatterns 中未找到 'yyyy'：{}", indexPatterns);
            return "";
        }

        String prefix = indexPatterns.substring(0, dateIndex);

        // 去除结尾的 -, _, 空格等
        return prefix.replaceAll("[-_\\s]+$", "");
    }

    /**
     * indexPatterns 格式有以下几种样式
     * lcc-logs-yyyy-*
     * lcc--yyyy-*
     * lcc--yyyy
     *
     * 清解析成
     *
     * lcc-logs-yyyy
     * lcc--yyyy
     * lcc--yyyy
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

        // 确保包含 yyyy
        if (!prefix.contains("yyyy")) {
            log.warn("indexPatterns 不包含 yyyy: {}", indexPatterns);
            return "";
        }

        return prefix;
    }

    public void handle() {
        // 创建未来2年的索引
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");

        // 1. 循环每个 indexPatterns,这里先是一个吧
        log.info("开始处理按年索引模式: {}", indexPatterns);
        for (int i = 1; i <= 2; i++) {
            // 计算未来年份
            Calendar futureCalendar = (Calendar) calendar.clone();
            futureCalendar.add(Calendar.YEAR, i);
            String futureDate = sdf.format(futureCalendar.getTime());

            // 创建未来模版信息
            createFeatureTemplate(futureDate);

            // 创建未来索引
            createFeatureIndex(futureDate);
        }
    }

    /**
     * 创建未来索引
     * 当前时间比如是 2025 那么未来索引就是 2026,2027
     * 我们创建未来2年的索引
     * lcc-logs-2026-0000001
     * lcc-logs-2027-0000001
     * 等等
     */
    private void createFeatureIndex(String futureDate) {
        try {
            // 1. 根据模版获取符合模版的索引列表
            String futurePattern = indexPatterns.replace("yyyy", futureDate);
            List<String> indices = elasticRealIndexService.listIndexNameByPrefix(futurePattern);

            // 2. 如果已经有未来年份的索引，跳过
            if (indices != null && !indices.isEmpty()) {
                log.info("未来年份 {} 的索引已存在，跳过创建", futureDate);
                return;
            }
            createFirstIndex(futureDate);
            log.info("年度索引创建完成: {}", futureDate);
        } catch (Exception e) {
            log.error("创建年度索引时发生错误: {}", e.getMessage(), e);
        }
    }

    private String generateNextIndexName(String currentIndexName) {
        // 假设索引格式为 aaa-xxx-yyyy-0000001
        Pattern pattern = Pattern.compile("(.*-\\d{4})-(\\d+)$");
        Matcher matcher = pattern.matcher(currentIndexName);
        
        if (matcher.find()) {
            String prefix = matcher.group(1);
            int sequence = Integer.parseInt(matcher.group(2));
            return String.format("%s-%07d", prefix, sequence + 1);
        } else {
            // 如果不符合预期格式，添加当前年份和序列号
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String currentYear = sdf.format(new Date());
            return String.format("%s-%s-%07d", currentIndexName, currentYear, 1);
        }
    }

    private String generateAliasName(String indexName) {
        // 从索引名称中提取别名，去掉最后的序列号部分
        Pattern pattern = Pattern.compile("(.*-\\d{4})-\\d+$");
        Matcher matcher = pattern.matcher(indexName);
        
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            // 如果不符合预期格式，使用索引名称作为别名基础
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
            String currentYear = sdf.format(new Date());
            return indexName + "-" + currentYear;
        }
    }

    /**
     * 创建未来模版
     */
    private void createFeatureTemplate(String futureDate) {
        try {
            // 2. 生成对应时间模版
            JSONObject templateJson = templatePreviewService.previewEffectTemplateByDate(esUnconvertedTemplate.getId(),futureDate);
            
            // 3. 创建模版信息
            String templateName = esUnconvertedTemplate.getEnName();
            elasticRealTemplateService.saveOrUpdateTemplate(templateName, templateJson);
            
            log.info("模板 {} 创建/更新成功", templateName);
        } catch (Exception e) {
            log.error("创建模板时发生错误: {}", e.getMessage(), e);
        }
    }
    
    
    /**
     * 创建第一个索引
     * 当没有查询到符合模式的索引时，创建初始索引
     */
    private void createFirstIndex(String futureDate) {
        try {
            // 1. 获取索引模式
            String indexPatterns = esUnconvertedTemplate.getIndexPatterns();
            
            log.info("未来年份: {}", futureDate);

            // 3. 提取前缀，去除 yyyy 和 *
            if(indexPatterns.endsWith("-*")){
                indexPatterns = indexPatterns.substring(0, indexPatterns.length() - 2);
            }else if(indexPatterns.endsWith("*") ) {
                indexPatterns = indexPatterns.substring(0, indexPatterns.length() - 1);
            }
            // ailpha-logs-yyyy 获取时间之前的前缀信息
            String prefix = indexPatterns.replace("-yyyy", "");
            prefix = prefix.replace("yyyy", "");
            log.info("前缀: {}", prefix);

            // 4. 生成第一个索引名称，格式为：ailpha-logs-2026-0000001
            String firstIndexName = String.format("%s-%s-%07d", prefix, futureDate, 1);
            log.info("生成第一个索引名称: {}", firstIndexName);

            // 5. 生成别名，格式为：ailpha-logs-2026
            String aliasName = String.format("%s-%s", prefix, futureDate);
            log.info("生成别名: {}", aliasName);

            // 6. 创建索引
            JSONObject templateJson = templatePreviewService.previewEffectTemplateByDate(esUnconvertedTemplate.getId(),futureDate);
            elasticRealIndexService.createIndex(firstIndexName, templateJson);

            // 7. 添加别名
            elasticAliansService.addAlias(firstIndexName, aliasName);

            // 8. 设置别名可写
            elasticAliansService.changeIndexWrite(firstIndexName, aliasName, true);

            log.info("成功创建第一个索引: {}, 别名: {}", firstIndexName, aliasName);
        } catch (Exception e) {
            log.error("创建第一个索引时发生错误: {}", e.getMessage(), e);
        }
    }
}