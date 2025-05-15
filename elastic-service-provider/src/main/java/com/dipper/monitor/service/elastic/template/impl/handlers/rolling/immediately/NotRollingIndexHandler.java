package com.dipper.monitor.service.elastic.template.impl.handlers.rolling.immediately;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 没有时间信息的索引
 * 比如这个索引是 log-xxx 这种
 * 那么生成的索引应该是 log-xxx-000001
 * 我现在要滚动索引，那么先生成这个的模版
 * 然后保存或者更新这个模版
 * 然后生成索引  log-xxx-000002
 * 然后重新设置这个索引的别名指向新的索引
 */
public class NotRollingIndexHandler extends AbstractRollingIndexByTemplateHandler {

    private static final Logger log = LoggerFactory.getLogger(NotRollingIndexHandler.class);


    // 索引模式，例如 log-xxx-*
    private String indexPatterns = null;
    // 索引前缀，例如 log-xxx
    private String indexPrefix = null;
    // 索引前缀加星号，例如 log-xxx*
    private String indexPrefixWithStar = null;

    public NotRollingIndexHandler(EsUnconvertedTemplate esUnconvertedTemplate) {
        super(esUnconvertedTemplate);


        indexPatterns = esUnconvertedTemplate.getIndexPatterns();
        indexPrefix = getIndexPrefix();
        indexPrefixWithStar = indexPrefix + "*";
    }

    /**
     * 获取索引前缀，去除尾部的特殊符号
     * 支持格式：
     * - log-xxx-*
     * - log-xxx*
     * - log-xxx
     *
     * @return 清理后的前缀，如 "log-xxx"
     */
    private String getIndexPrefix() {
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

        return prefix;
    }

    public void handle() {
        log.info("开始处理索引模式: {}", indexPatterns);
        
        // 创建或更新模板
        createTemplate();
        
        // 滚动索引
        rollIndex();
    }

    private void rollIndex() {
        try {
            // 1. 根据模式获取符合模式的索引列表
            List<String> indices = elasticRealIndexService.listIndexNameByPrefix(indexPatterns);
            
            // 2. 如果没有索引，创建第一个索引
            if (indices == null || indices.isEmpty()) {
                log.info("没有找到符合模式 {} 的索引，创建第一个索引", indexPatterns);
                createFirstIndex();
                return;
            }
            
            // 3. 排序并获取当前最新索引，例如 log-xxx-0000001
            indices.sort((a, b) -> b.compareTo(a)); // 降序排序，最新的索引在前面
            String currentLatestIndex = indices.get(0);
            log.info("当前最新索引: {}", currentLatestIndex);
            
            // 4. 生成新的索引名称（当前最新索引+1），例如 log-xxx-0000002
            String newIndexName = generateNextIndexName(currentLatestIndex);
            log.info("新生成的索引名称: {}", newIndexName);
            
            // 5. 生成别名，例如 log-xxx
            String aliasName = generateAliasName(currentLatestIndex);
            log.info("生成的别名: {}", aliasName);
            
            // 6. 获取所有相关别名
            List<String> aliases = elasticAliansService.listAliansByIndexPatterns(indexPrefixWithStar);
            
            // 7. 设置现有别名为只读，并结束生命周期
            for (String alias : aliases) {
                elasticAliansService.setAliasReadOnly(alias);
                elasticRealLifecyclePoliciesService.lifeCycleEnd(alias);
            }
            
            // 8. 创建新索引并设置别名
            JSONObject templateJson = templatePreviewService.previewEffectTemplate(esUnconvertedTemplate.getId());
            elasticClientService.createIndex(newIndexName, templateJson);
            elasticAliansService.addAlias(newIndexName, aliasName);
            
            // 9. 设置新索引别名为可写
            elasticAliansService.changeIndexWrite(newIndexName, aliasName, true);
            
            log.info("索引滚动完成: {} -> {}", currentLatestIndex, newIndexName);
        } catch (Exception e) {
            log.error("滚动索引时发生错误: {}", e.getMessage(), e);
        }
    }

    private String generateNextIndexName(String currentIndexName) {
        // 假设索引格式为 log-xxx-0000001
        Pattern pattern = Pattern.compile("(.*?)-(\\d+)$");
        Matcher matcher = pattern.matcher(currentIndexName);
        
        if (matcher.find()) {
            String prefix = matcher.group(1);
            int sequence = Integer.parseInt(matcher.group(2));
            return String.format("%s-%07d", prefix, sequence + 1);
        } else {
            // 如果不符合预期格式，使用索引前缀加序列号
            return String.format("%s-%07d", indexPrefix, 1);
        }
    }

    private String generateAliasName(String indexName) {
        // 从索引名称中提取别名，去掉最后的序列号部分
        Pattern pattern = Pattern.compile("(.*?)-(\\d+)$");
        Matcher matcher = pattern.matcher(indexName);
        
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            // 如果不符合预期格式，使用索引前缀作为别名
            return indexPrefix;
        }
    }

    private void createTemplate() {
        try {
            // 1. 生成模板
            JSONObject templateJson = templatePreviewService.previewEffectTemplate(esUnconvertedTemplate.getId());
            
            // 2. 创建或更新模板
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
    private void createFirstIndex() {
        try {
            // 1. 获取索引前缀
            String prefix = indexPrefix;
            log.info("索引前缀: {}", prefix);

            // 2. 生成第一个索引名称，格式为：log-xxx-0000001
            String firstIndexName = String.format("%s-%07d", prefix, 1);
            log.info("生成第一个索引名称: {}", firstIndexName);

            // 3. 生成别名，格式为：log-xxx
            String aliasName = prefix;
            log.info("生成别名: {}", aliasName);

            // 4. 创建索引
            JSONObject templateJson = templatePreviewService.previewEffectTemplate(esUnconvertedTemplate.getId());
            elasticClientService.createIndex(firstIndexName, templateJson);

            // 5. 添加别名
            elasticAliansService.addAlias(firstIndexName, aliasName);

            // 6. 设置别名可写
            elasticAliansService.changeIndexWrite(firstIndexName, aliasName, true);

            log.info("成功创建第一个索引: {}, 别名: {}", firstIndexName, aliasName);
        } catch (Exception e) {
            log.error("创建第一个索引时发生错误: {}", e.getMessage(), e);
        }
    }
}