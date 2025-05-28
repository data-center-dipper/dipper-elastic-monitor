package com.dipper.monitor.entity.elastic.slowsearch.slow.index;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlowQueryAnalysisResult {
    private String queryId;
    private String indexName;
    private String originalQuery;
    private String optimizedQuery;
    private long originalExecutionTime;
    private long optimizedExecutionTime;
    private String issueType;
    private String impactLevel;
    private String explanation;
}