CREATE TABLE t_life_policy (
    id INT AUTO_INCREMENT PRIMARY KEY,
    zh_name VARCHAR(255) NOT NULL COMMENT '中文名称',
    en_name VARCHAR(255) NOT NULL COMMENT '英文名称',
    policy_value TEXT NOT NULL COMMENT 'JSON格式的策略内容',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY unique_en_name (en_name) COMMENT '英文名称唯一'
) COMMENT='生命周期策略表';

ALTER TABLE elastic_monitor.t_elastic_template ADD life_policy varchar(100) NULL;
ALTER TABLE elastic_monitor.t_elastic_template ADD rolling_period INT NULL;
ALTER TABLE elastic_monitor.t_elastic_cluster ADD cluster_version varchar(100) NULL;


CREATE TABLE `t_task_metadata` (
    `id` BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',

    `annotation_type` VARCHAR(255) NOT NULL COMMENT '注解类型（如@Scheduled）',
    `class_name` VARCHAR(255) NOT NULL COMMENT '类名',
    `method_name` VARCHAR(255) NOT NULL COMMENT '方法名',
    `cron` VARCHAR(255) DEFAULT NULL COMMENT 'cron 表达式',
    `fixed_rate` BIGINT DEFAULT NULL COMMENT '固定执行间隔（毫秒）',
    `fixed_delay` BIGINT DEFAULT NULL COMMENT '固定延迟执行时间（毫秒）',

    `author` VARCHAR(100) DEFAULT NULL COMMENT '作者',
    `group_name` VARCHAR(255) DEFAULT NULL COMMENT '任务组名',
    `job_desc` TEXT DEFAULT NULL COMMENT '任务描述',
    `edit_able` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可编辑（0-不可编辑，1-可编辑）',

    `additional_attributes` TEXT DEFAULT NULL COMMENT '附加属性（JSON 格式存储）',

    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 索引建议
    INDEX idx_annotation_type (`annotation_type`),
    INDEX idx_class_method (`class_name`, `method_name`),
    INDEX idx_group_name (`group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='注解元数据表';