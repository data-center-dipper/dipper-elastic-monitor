package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.query;

import com.alibaba.fastjson.JSONObject;

public class GenerallOptimizationHandler  extends  AbstractOptimizationHandler  {
    public GenerallOptimizationHandler(JSONObject setting, JSONObject mappings) {
        super(setting, mappings);
    }

    public void generateDefaultOptimization(JSONObject optimizationResult, JSONObject mappings) {
        result.put("issueType", "查询性能问题");
        result.put("impact", "中");
        result.put("optimizedTime", 3500); // 预计优化后时间

        result.put("suggestion", "1. 检查查询是否使用了适当的索引\n2. 减少返回字段，只选择必要的字段\n3. 添加适当的过滤条件限制结果集");

        JSONObject optimizedQuery = new JSONObject();
        JSONObject source = new JSONObject();
        source.put("includes", new String[]{"id", "name", "timestamp"});
        optimizedQuery.put("_source", source);
        result.put("optimizedQuery", optimizedQuery);

        result.put("explanation", "通过限制返回字段和添加适当的过滤条件，可以减少需要处理和传输的数据量，从而提高查询性能。确保查询使用了适当的索引也是提高性能的关键。");
    }
}
