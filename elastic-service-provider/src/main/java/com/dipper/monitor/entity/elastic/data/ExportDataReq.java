package com.dipper.monitor.entity.elastic.data;

import lombok.Data;

/**
 *  index 索引名称
 *  query 查询条件（JSON格式）
 *  format 导出格式（json/csv）
 *  size 导出条数
 */
@Data
public class ExportDataReq {
    private String index ;
    private String query ;
    private String format;
    private int size = 1000;
}
