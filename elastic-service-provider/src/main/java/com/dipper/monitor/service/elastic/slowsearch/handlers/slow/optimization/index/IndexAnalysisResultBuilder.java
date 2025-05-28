package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index;


import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.slowsearch.slow.index.IndexSettingOptimization;
import com.dipper.monitor.entity.elastic.slowsearch.slow.index.IndexSlowAnalysisResult;
import com.dipper.monitor.entity.elastic.slowsearch.slow.index.SlowQueryAnalysisResult;

import java.util.List;
import java.util.Map;

public class IndexAnalysisResultBuilder {

    public IndexSlowAnalysisResult build(String indexName,
                                         JSONObject setting,
                                         JSONObject mappings,
                                         List<SlowQueryAnalysisResult> analysisResults,
                                         List<IndexSettingOptimization> settingOptimizations,
                                         Map<String, Integer> issueTypeCounts) {
        IndexSlowAnalysisResult result = new IndexSlowAnalysisResult();
        result.setIndexName(indexName);
        result.setIndexSetting(setting);
        result.setIndexMapping(mappings);
        result.setSlowQueryResults(analysisResults);
        result.setSettingOptimizations(settingOptimizations);
        result.setIssueTypeCounts(issueTypeCounts);
        return result;
    }
}