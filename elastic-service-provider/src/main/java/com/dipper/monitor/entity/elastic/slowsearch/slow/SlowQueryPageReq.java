package com.dipper.monitor.entity.elastic.slowsearch.slow;

import lombok.Data;

import java.util.Date;

/**
 * 慢查询分页请求参数
 */
@Data
public class SlowQueryPageReq {
    private Integer pageNum = 1;      // 页码
    private Integer pageSize = 10;    // 每页大小
    private Integer offset = 10;    // Offset 数据库字段

    private Date startTime;    // 开始时间
    private Date endTime;    // 结束时间

    private String searchText;        // 搜索关键词
    private String queryType;         // 查询类型

    private String status;            // 状态
    private String indexName;         // 索引名称
    private String nodeName;         // node

    private Long minExecutionTime;    // 最小执行时间
    private Long maxExecutionTime;    // 最大执行时间
}