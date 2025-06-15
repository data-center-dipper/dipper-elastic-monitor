package com.dipper.monitor.entity.elastic.data.migration;

import lombok.Data;

/**
 * 数据迁移任务请求参数实体类
 * 用于接收用户提交的跨集群数据迁移任务配置信息
 */
@Data
public class MigrationTaskReq {

    /**
     * 主键 ID（可选）
     * 如果是编辑或更新任务时使用
     */
    private Integer id;

    // 任务唯一ID
    private String taskId;

    /**
     * 源集群 ID
     * 表示要从哪个 Elasticsearch 集群中迁移数据
     */
    private String sourceClusterId;

    /**
     * 目标集群 ID
     * 表示数据将迁移到哪个 Elasticsearch 集群
     */
    private String targetClusterId;

    /**
     * 索引匹配模式
     * 示例：xxx-*，表示匹配所有以 xxx- 开头的索引
     */
    private String indexPattern;

    /**
     * 查询条件（JSON 格式）
     * 用户自定义的 ES 查询语句，用于过滤需要迁移的数据
     * 示例：{"query": {"term": {"type": "log"}}}
     */
    private String queryCondition;

    /**
     * 迁移粒度
     * 可选值如：hourly（按小时）、daily（按天）、custom（自定义小时数）
     * 对应页面下拉选择框的值
     */
    private String granularity;

    /**
     * 是否为一次性任务
     * true：执行一次后不再重复
     * false：可能支持周期性任务（当前是否启用需看业务逻辑）
     */
    private Boolean isOnceExecution;

    /**
     * 目标索引前缀
     * 数据迁移到目标集群时，可指定新的索引名前缀
     * 示例：如果原索引是 xxx-2025-06-01，目标前缀为 backup-xxx，则新索引为 backup-xxx-2025-06-01
     */
    private String targetIndexPrefix;

    /**
     * 执行策略
     * 异常处理方式：
     * - "abort"：遇到异常终止整个任务
     * - "continue"：遇到异常跳过并继续执行下一个子任务
     */
    private String executePolicy;

    /**
     * 并发限制
     * 控制同时执行的子任务数量上限
     * 示例：设置为 3，表示最多同时执行 3 个子任务
     */
    private Integer concurrencyLimit;
}