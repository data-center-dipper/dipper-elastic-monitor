package com.dipper.monitor.service.elastic.template.impl.handlers.rolling.feature;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
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
 * 半年滚动的模版
 * 索引模版 格式 lcc-logs-yyyyMM-*
 * 上半年 1-6月
 *       格式 lcc-logs-yyyyMM-*
 *       索引
 *          lcc-logs-202501-0000001
 *          lcc-logs-202501-0000002
 *          lcc-logs-202501-0000003
 * 下半年 7-12月
 *       格式 lcc-logs-yyyyMM-*
 *       索引
 *          lcc-logs-202507-0000001
 *          lcc-logs-202507-0000002
 *          lcc-logs-202507-0000003
 */
public class HafYearFeatureIndexHandler extends AbstractFeatureIndexHandler {

    private static final Logger log = LoggerFactory.getLogger(HafYearFeatureIndexHandler.class);

    // 对应的日期
    private String futureDate = null;
    // 索引模版 格式 lcc-logs-yyyyMM-*
    private String indexPatterns = null;
    // 索引前缀 带时间 lcc-logs-yyyyMM
    private String indexPatternsPrefixHaveDate = null;
    // 索引前缀 不带时间 lcc-logs-
    private String indexPatternsPrefixNoDate = null;
    // 索引前缀 不带时间,增加*号 lcc-logs-* 或者 lcc-logs*
    private String indexPatternsPrefixNoDateAddXing = null;
    // 索引前缀 带时间 lcc-logs-202501
    private String indexPatternsPrefixRealDate = null;
    // 索引前缀 带时间,增加*号 lcc-logs-202501*
    private String indexPatternsPrefixRealDateAddXing = null;

    public HafYearFeatureIndexHandler(EsUnconvertedTemplate esUnconvertedTemplate, String futureDate) {
        super(esUnconvertedTemplate);

        this.futureDate = futureDate;
        indexPatterns = esUnconvertedTemplate.getIndexPatterns();
        indexPatternsPrefixHaveDate = getIndexPrefixHaveDate();
        indexPatternsPrefixNoDate = getIndexPrefixNoDateAndTail();
        indexPatternsPrefixNoDateAddXing = indexPatternsPrefixNoDate + "*";
        indexPatternsPrefixRealDate = indexPatternsPrefixHaveDate.replace("yyyyMM", futureDate);
        indexPatternsPrefixRealDateAddXing = indexPatternsPrefixHaveDate.replace("yyyyMM", futureDate) + "*";
    }

    /**
     * indexPatterns 格式有以下几种样式
     * lcc-logs-yyyyMM-*
     * lcc--yyyyMM-*
     * lcc--yyyyMM
     *
     * 清解析成
     *
     * lcc-logs-yyyyMM
     * lcc--yyyyMM
     * lcc--yyyyMM
     *
     * @return
     */
    public String getIndexPrefixHaveDate() {
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

        // 确保包含 yyyyMM
        if (!prefix.contains("yyyyMM")) {
            log.warn("indexPatterns 不包含 yyyyMM: {}", indexPatterns);
            return "";
        }

        return prefix;
    }

    /**
     * 获取索引前缀，去除 yyyyMM 时间格式部分，并清理结尾的特殊符号。
     * 支持格式：
     * - lcc-logs-yyyyMM-*
     * - lcc-yyyyMM-*
     * - lcc-yyyyMM
     *
     * @return 清理后的前缀，如 "lcc-logs" 或 "lcc"
     */
    public String getIndexPrefixNoDateAndTail() {
        if (indexPatterns == null || indexPatterns.isEmpty()) {
            log.error("indexPatterns 为空");
            return "";
        }

        int dateIndex = indexPatterns.indexOf("yyyyMM");
        if (dateIndex == -1) {
            log.warn("indexPatterns 中未找到 'yyyyMM'：{}", indexPatterns);
            return "";
        }

        String prefix = indexPatterns.substring(0, dateIndex);

        // 去除结尾的 -, _, 空格等
        return prefix.replaceAll("[-_\\s]+$", "");
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
     * 半年滚动索引，根据当前月份确定下一个半年的索引
     * 上半年(1-6月)使用1月: lcc-logs-202501-000001
     * 下半年(7-12月)使用7月: lcc-logs-202507-000001
     */
    private void createFeatureIndex() {
        try {
            // 1. 根据模版获取符合模版的索引列表
            log.info("根据模版获取符合模版的索引列表: {}", indexPatternsPrefixRealDateAddXing);
            List<String> indices = elasticRealIndexService.listIndexNameByPrefix(indexPatternsPrefixRealDateAddXing);

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

    /**
     * 创建未来模版
     */
    private void createFeatureTemplate() throws IOException {
        // 2. 生成对应时间模版
        JSONObject templateJson = templatePreviewService.previewEffectTemplateByDate(esUnconvertedTemplate.getId(), futureDate);

        // 3. 创建模版信息
        String templateName = indexPatternsPrefixRealDate;
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

            // 3. 提取前缀，去除 yyyyMM 和 *
            if (indexPatterns.endsWith("-*")) {
                indexPatterns = indexPatterns.substring(0, indexPatterns.length() - 2);
            } else if (indexPatterns.endsWith("*")) {
                indexPatterns = indexPatterns.substring(0, indexPatterns.length() - 1);
            }
            // ailpha-logs-yyyyMM 获取时间之前的前缀信息
            String prefix = indexPatterns.replace("-yyyyMM", "");
            prefix = prefix.replace("yyyyMM", "");
            log.info("前缀: {}", prefix);

            // 4. 生成第一个索引名称，格式为：ailpha-logs-202501-0000001
            String firstIndexName = String.format("%s-%s-%07d", prefix, futureDate, 1);
            log.info("生成第一个索引名称: {}", firstIndexName);

            // 5. 生成别名，格式为：ailpha-logs-202501
            String aliasName = String.format("%s-%s", prefix, futureDate);
            log.info("生成别名: {}", aliasName);

            // 6. 创建索引
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
