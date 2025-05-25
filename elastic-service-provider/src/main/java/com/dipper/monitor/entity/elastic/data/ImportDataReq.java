package com.dipper.monitor.entity.elastic.data;

import lombok.Data;

/**
 *  index 索引名称
 *  format 导出格式（json/csv）
 */
@Data
public class ImportDataReq {
    private String index;
    private String format;
    private String filePath;
}
