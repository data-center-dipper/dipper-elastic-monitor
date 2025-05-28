package com.dipper.monitor.entity.elastic.slowsearch.slow.index;

import lombok.Data;

import java.util.Date;

/**
 * 索引优化统计请求
 */
@Data
public class IndexOptimizationSummaryReq {
    // 开始时间
    private Date startTime;
    // 结束时间
    private Date endTime;
}