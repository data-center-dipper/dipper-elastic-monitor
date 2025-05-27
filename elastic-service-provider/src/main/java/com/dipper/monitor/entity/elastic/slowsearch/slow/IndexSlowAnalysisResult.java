package com.dipper.monitor.entity.elastic.slowsearch.slow;



import lombok.Data;

@Data
public class IndexSlowAnalysisResult {
    private String originalQuery;
    private String optimizedQuery;
    private Long originalExecutionTime;
    private Long optimizedExecutionTime;
    private String issueType;
    private String impactLevel;
    private String explanation;
}