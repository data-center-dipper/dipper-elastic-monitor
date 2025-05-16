package com.dipper.monitor.service.elastic.template.impl.handlers.rolling.feature;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.template.impl.handlers.rolling.immediately.AbstractRollingIndexByTemplateHandler;
import com.dipper.monitor.utils.elastic.IndexPatternsUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 按照天滚动的模版,生成未来索引
 * 索引模版 格式 lcc-logs-yyyyMMdd-*
 * 比如 lcc-logs-20230201-*
 * 那么这个对应的索引是 lcc-logs-20230201-0000001
 * 那么滚动一次变成 lcc-logs-20230201-0000002
 */
public class DaysOfFeatureIndexHandler extends AbstractFeatureIndexHandler {

    private static final Logger log = LoggerFactory.getLogger(DaysOfFeatureIndexHandler.class);


    // 对应的日期
    private String futureDate = null;
    // 索引模版 格式 lcc-logs-yyyyMMdd-*
    private String indexPatterns = null;
    // 索引前缀 带时间 lcc-logs-yyyyMMdd
    private String indexPatternsPrefixHaveDate = null;
    // 索引前缀 不带时间 lcc-logs-
    private String indexPatternsPrefixNoDate = null;
    // 索引前缀 不带时间,增加*号 lcc-logs-* 或者 lcc-logs*
    private String indexPatternsPrefixNoDateAddXing = null;
    // 索引前缀 不带时间,增加*号 lcc-logs-20250101*
    private String indexPatternsPrefixDateAddXing = null;

    public DaysOfFeatureIndexHandler(EsUnconvertedTemplate esUnconvertedTemplate, String futureDate) {
        super(esUnconvertedTemplate);

        this.futureDate = futureDate;
        indexPatterns = esUnconvertedTemplate.getIndexPatterns();
        indexPatternsPrefixHaveDate = IndexPatternsUtils.getIndexPrefixHaveDate(indexPatterns);
        indexPatternsPrefixNoDate = IndexPatternsUtils.getIndexPrefixNoDateAndTail(indexPatterns);
        indexPatternsPrefixNoDateAddXing = indexPatternsPrefixNoDate + "*";
        indexPatternsPrefixDateAddXing = indexPatternsPrefixHaveDate.replace("yyyyMMdd", futureDate) + "*";

    }


    public void handle() throws IOException {
        // 1. 循环每个 indexPatterns,这里先是一个吧
        log.info("开始处理索引模式: {}", indexPatterns);

        // 创建未来模版信息
        createFeatureTemplate();

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
        try {
            // 1. 根据模版获取符合模版的索引列表
            log.info("根据模版获取符合模版的索引列表: {}", indexPatternsPrefixDateAddXing);
            List<String> indices = elasticRealIndexService.listIndexNameByPrefix(indexPatternsPrefixDateAddXing);

            // 2. 如果有索引，跳过
            if (indices != null && !indices.isEmpty()) {
                log.info("找到符合模式 {} 的索引，跳过滚动操作", indexPatterns);
                return;
            }
            createFirstIndex();
            log.info("索引滚动完成");
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

    /**
     * 创建未来模版
     */
    private void createFeatureTemplate() throws IOException {
            // 2. 生成对应时间模版
        JSONObject templateJson = templatePreviewService.previewEffectTemplateByDate(esUnconvertedTemplate.getId(), futureDate);

        // 3. 创建模版信息
        String templateName = esUnconvertedTemplate.getEnName();
        elasticRealTemplateService.saveOrUpdateTemplate(templateName, templateJson);

        log.info("模板 {} 创建/更新成功", templateName);
    }


    /**
     * 创建第一个索引
     * 当没有查询到符合模式的索引时，创建初始索引
     */
    private void createFirstIndex() {
        try {
            // 1. 获取索引模式
            String indexPatterns = esUnconvertedTemplate.getIndexPatterns();

            log.info("未来日期: {}", futureDate);

            // 3. 提取前缀，去除 yyyyMMdd 和 *
            if (indexPatterns.endsWith("-*")) {
                indexPatterns = indexPatterns.substring(0, indexPatterns.length() - 2);
            } else if (indexPatterns.endsWith("*")) {
                indexPatterns = indexPatterns.substring(0, indexPatterns.length() - 1);
            }
            // ailpha-logs-yyyyMMdd 获取时间之前的前缀信息
            String prefix = indexPatterns.replace("-yyyyMMdd", "");
            prefix = prefix.replace("yyyyMMdd", "");
            log.info("前缀: {}", prefix);

            // 4. 生成第一个索引名称，格式为：ailpha-logs-20250515-0000001
            String firstIndexName = String.format("%s-%s-%07d", prefix, futureDate, 1);
            log.info("生成第一个索引名称: {}", firstIndexName);

            // 5. 生成别名，格式为：ailpha-logs-20250515
            String aliasName = String.format("%s-%s", prefix, futureDate);
            log.info("生成别名: {}", aliasName);

            // 6. 创建索引
//            JSONObject templateJson = templatePreviewService.previewEffectTemplateByDate(esUnconvertedTemplate.getId(), futureDate);
//            elasticRealIndexService.createIndex(firstIndexName,templateJson);
            elasticRealIndexService.createIndex(firstIndexName);

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
