package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.query;

import com.alibaba.fastjson.JSONObject;

public class ScrollOptimizationHandler extends  AbstractOptimizationHandler  {

    public ScrollOptimizationHandler(JSONObject setting, JSONObject mappings) {
        super(setting, mappings);
    }

    public void generateScrollQueryOptimization(JSONObject optimizationResult, JSONObject mappings) {
        result.put("issueType", "滚动查询性能问题");
        result.put("impact", "中");
        result.put("optimizedTime", 2800); // 预计优化后时间

        StringBuilder suggestion = new StringBuilder();
        suggestion.append("1. 增加 scroll_size 参数值以减少请求次数\n")
                .append("2. 减少返回字段，只选择必要的字段\n")
                .append("3. 考虑使用 search_after 替代 scroll API");

        if (setting != null && setting.containsKey("numberOfShards")) {
            int shards = setting.getIntValue("numberOfShards");
            if (shards > 10) {
                suggestion.append("\n4. 当前分片数较多，可能影响 scroll 性能，建议评估是否合理");
            }
        }

        result.put("suggestion", suggestion.toString());

        // 示例优化查询
        JSONObject optimizedQuery = new JSONObject();
        optimizedQuery.put("size", 1000);
        JSONObject source = new JSONObject();
        source.put("includes", new String[]{"id", "name", "timestamp"});
        optimizedQuery.put("_source", source);
        optimizedQuery.put("sort", new String[]{"_doc"});

        result.put("optimizedQuery", optimizedQuery);
        result.put("explanation", "滚动查询在处理大量结果时会消耗服务器资源并保持上下文。通过增加每批次的文档数量，可以减少总请求次数；通过只返回必要字段，可以减少网络传输和内存使用。对于新版本的Elasticsearch，search_after通常是一个更高效的替代方案，因为它不需要维护滚动上下文。");
    }
}
