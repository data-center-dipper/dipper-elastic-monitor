package com.dipper.monitor.entity.elastic.slowsearch.slow;

import lombok.Data;

import java.util.Date;

@Data
public class SlowQuerySummaryReq {
    private Date startTime;    // 开始时间
    private Date endTime;    // 结束时间

}
