package com.dipper.monitor.entity.db.elastic;

import lombok.Data;

import java.util.Date;

@Data
public class IndexWriteEntity {
    // 模板名称
    private String templateName;

    // 索引名称
    private String indexName;

    // 别名
    private String alias;

    // 是否可写
    private boolean canWrite;

    // 字段数量
    private int fieldCount;

    // 是否包含特殊字符
    private boolean hasSpecialChar;

    // 写入速率（条/秒）
    private double writeRate;

    // 文档总数
    private long docCount;

    // 创建时间
    private Date createTime;

    public IndexWriteEntity() {
    }

    public IndexWriteEntity(String templateName, String indexName, String alias, boolean canWrite, int fieldCount, boolean hasSpecialChar, double writeRate, long docCount, Date createTime) {
        this.templateName = templateName;
        this.indexName = indexName;
        this.alias = alias;
        this.canWrite = canWrite;
        this.fieldCount = fieldCount;
        this.hasSpecialChar = hasSpecialChar;
        this.writeRate = writeRate;
        this.docCount = docCount;
        this.createTime = createTime;
    }
}