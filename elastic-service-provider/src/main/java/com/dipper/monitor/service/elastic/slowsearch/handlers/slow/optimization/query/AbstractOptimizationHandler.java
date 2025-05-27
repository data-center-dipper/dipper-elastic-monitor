package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.query;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractOptimizationHandler {
    
    protected final JSONObject setting;
    protected final JSONObject mappings;
    protected JSONObject result;
    
    public AbstractOptimizationHandler(JSONObject setting, JSONObject mappings) {
        this.setting = setting;
        this.mappings = mappings;
    }
    
    /**
     * 分析索引设置并添加到结果中
     */
    protected void analyzeIndexSettings(JSONObject result) {
        if (setting == null || setting.isEmpty()) return;

        JSONObject indexSetting = setting.getJSONObject("index");

        int numberOfShards = Integer.parseInt(indexSetting.getString("number_of_shards"));
        int numberOfReplicas = Integer.parseInt(indexSetting.getString("number_of_replicas"));

        result.put("numberOfShards", numberOfShards);
        result.put("numberOfReplicas", numberOfReplicas);

        StringBuilder settingAnalysis = new StringBuilder();

        if (numberOfReplicas > 2) {
            settingAnalysis.append("当前索引副本数较多，可能会影响写入性能；若读多写少可保留。");
        }

        if (numberOfShards > 10 && numberOfShards < 100) {
            settingAnalysis.append("当前分片数较高，可能影响查询效率，建议根据数据量调整。");
        }

        if (settingAnalysis.length() > 0) {
            result.put("settingAnalysis", settingAnalysis.toString());
        }
    }

    /**
     * 分析索引映射并添加到结果中
     */
    protected void analyzeIndexMappings(JSONObject result) {
        if (mappings == null || mappings.isEmpty()) return;

        JSONObject properties = mappings.getJSONObject("mappings").getJSONObject("properties");

        StringBuilder fieldAnalysis = new StringBuilder();

        for (String fieldName : properties.keySet()) {
            JSONObject fieldDef = properties.getJSONObject(fieldName);
            String type = fieldDef.getString("type");

            if ("text".equals(type)) {
                fieldAnalysis.append("字段 ").append(fieldName).append(" 是 text 类型，全文检索可能导致性能问题，建议使用 .keyword 进行精确匹配。\n");
            }

            if ("nested".equals(type)) {
                fieldAnalysis.append("字段 ").append(fieldName).append(" 是 nested 类型，查询需使用 nested 查询语法，否则可能导致性能下降。\n");
            }
        }

        if (fieldAnalysis.length() > 0) {
            result.put("mappingAnalysis", fieldAnalysis.toString());
        }
    }
    
    /**
     * 创建错误响应
     */
    protected String createErrorResponse(String errorMessage) {
        JSONObject error = new JSONObject();
        error.put("error", errorMessage);
        return error.toJSONString();
    }
    
    /**
     * 初始化优化结果对象
     */
    protected JSONObject initializeResult() {
        JSONObject result = new JSONObject();
        // 分析索引元数据
        analyzeIndexSettings(result);
        analyzeIndexMappings(result);
        return result;
    }
}
