package com.dipper.monitor.entity.elastic.slowsearch.kill;

import lombok.Data;

import java.util.Date;

@Data
public class KillPageReq {
    private Integer pageNum = 1;      // 页码
    private Integer pageSize = 10;    // 每页大小
    private Integer offset = 10;    // Offset 数据库字段

    private Date startTime;    // 开始时间
    private Date endTime;    // 结束时间

    private String searchText;        // 搜索关键词
}
