package com.dipper.monitor.entity.elastic.slowsearch.slow.index;



import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexSlowAnalysisResult {
    private String indexName;
    private JSONObject indexSetting;
    private JSONObject indexMapping;
    private List<SlowQueryAnalysisResult> slowQueryResults;
    private List<IndexSettingOptimization> settingOptimizations;
    private Map<String, Integer> issueTypeCounts;
}