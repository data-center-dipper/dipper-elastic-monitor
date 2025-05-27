package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.query;

import com.alibaba.fastjson.JSONObject;

public class SearchOptimizationHandler extends AbstractOptimizationHandler {
    
    public SearchOptimizationHandler(JSONObject setting, JSONObject mappings) {
        super(setting, mappings);
    }
    
    public void generateSearchQueryOptimization(JSONObject result, JSONObject mappings) {
        result.put("issueType", "全文检索性能问题");
        result.put("impact", "高");
        result.put("optimizedTime", 2200); // 预计优化后时间

        StringBuilder suggestion = new StringBuilder();
        suggestion.append("1. 使用字段级查询替代全文检索\n")
                .append("2. 添加适当的过滤条件限制结果集\n")
                .append("3. 考虑使用keyword类型字段进行精确匹配");

        if (result.containsKey("mappingAnalysis")) {
            suggestion.append("\n4. ").append(result.getString("mappingAnalysis").replace("\n", "\n4. "));
        }

        result.put("suggestion", suggestion.toString());

        // 示例优化查询
        JSONObject optimizedQuery = new JSONObject();
        JSONObject bool = new JSONObject();
        JSONObject must = new JSONObject();
        JSONObject match = new JSONObject();
        match.put("field.keyword", "value");
        must.put("match", match);
        bool.put("must", must);

        JSONObject filter = new JSONObject();
        JSONObject term = new JSONObject();
        term.put("status", "active");
        filter.put("term", term);
        bool.put("filter", filter);

        optimizedQuery.put("bool", bool);
        result.put("optimizedQuery", optimizedQuery);

        result.put("explanation", "全文检索查询通常会消耗大量资源，特别是在大型索引上。通过使用字段级查询和过滤器，可以显著提高查询性能。添加适当的过滤条件可以减少需要处理的文档数量，从而减少内存使用和提高响应速度。");
    }
}
