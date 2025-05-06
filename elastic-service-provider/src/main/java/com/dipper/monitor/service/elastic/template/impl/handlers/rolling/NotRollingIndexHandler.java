package com.dipper.monitor.service.elastic.template.impl.handlers.rolling;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.template.unconverted.EsUnconvertedTemplate;
import com.dipper.monitor.service.elastic.template.ElasticRealTemplateService;
import com.dipper.monitor.service.elastic.template.ElasticStoreTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private EsUnconvertedTemplate esUnconvertedTemplate;
    private ElasticStoreTemplateService elasticStoreTemplateService; // 假设这是你的服务类，用于处理模板相关的逻辑

    private ElasticRealTemplateService elasticRealTemplateService;

    public NotRollingIndexHandler(EsUnconvertedTemplate esUnconvertedTemplate,
                                  ElasticStoreTemplateService elasticStoreTemplateService,
                                  ElasticRealTemplateService elasticRealTemplateService) {
        this.esUnconvertedTemplate = esUnconvertedTemplate;
        this.elasticStoreTemplateService = elasticStoreTemplateService;
        this.elasticRealTemplateService = elasticRealTemplateService;
    }


    public void handle() throws Exception {
        String indexPatterns = esUnconvertedTemplate.getIndexPatterns();
        String enName = esUnconvertedTemplate.getEnName();
        log.info("开始处理索引模式: {}", indexPatterns);

        // 生成新的模板信息
        JSONObject templateJson = elasticRealTemplateService.previewTemplate(esUnconvertedTemplate);

        // 保存或更新模板
        elasticRealTemplateService.saveOrUpdateTemplate(enName,templateJson);

        // 获取当前最大的索引名称
        String currentMaxIndexName = getCurrentMaxIndexName(indexPatterns);
        log.info("当前最大索引名称：{}", currentMaxIndexName);

        // 生成下一个索引名称
        String newIndexName = generateNextIndexName(currentMaxIndexName);
        log.info("下一个索引名称：{}", newIndexName);

        // 创建新索引
        createNewIndex(newIndexName, templateJson);

        // 更新别名指向新的索引
        updateAliasesToNewIndex(newIndexName, currentMaxIndexName);
    }




    private String getCurrentMaxIndexName(String indexPattern) {
        // 获取当前最大的索引名称
        // 需要根据indexPattern来查询现有的索引，并找到最大的那个
        return "log-xxx-000001"; // 示例返回值，实际需要从Elasticsearch中获取
    }

    private String generateNextIndexName(String currentIndexName) {
        // 根据当前索引名称生成下一个索引名称
        String[] parts = currentIndexName.split("-");
        int nextNumber = Integer.parseInt(parts[parts.length - 1]) + 1;
        return String.format("%s-%06d", currentIndexName.substring(0, currentIndexName.lastIndexOf("-")), nextNumber);
    }

    private void createNewIndex(String newIndexName, JSONObject templateJson) throws Exception {
        // 使用模板JSON创建新索引
        // 可能需要调用Elasticsearch REST API来创建索引
    }

    private void updateAliasesToNewIndex(String newIndexName, String oldIndexName) throws Exception {
        // 更新别名，使它们指向新的索引
        // 需要使用Elasticsearch的API来更新别名
    }

    private boolean checkTemplateExistence(String templateName) throws Exception {
        // 检查模板是否存在
        // 返回true如果模板存在，否则false
        return false; // 示例返回值，实际需要从Elasticsearch中获取
    }
}