CREATE TABLE t_elastic_life_policy (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cluster_code VARCHAR(255) NOT NULL, -- 集群代码
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


CREATE TABLE index_write_stat (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    template_name VARCHAR(255) NOT NULL COMMENT '模板名称',
    index_name VARCHAR(255) NOT NULL COMMENT '索引名称',
    alias VARCHAR(255) COMMENT '别名',
    can_write TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否可写（0否，1是）',
    field_count INT NOT NULL COMMENT '字段数量',
    has_special_char TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否包含特殊字符（0否，1是）',
    write_rate DOUBLE NOT NULL COMMENT '写入速率（条/秒）',
    doc_count BIGINT NOT NULL COMMENT '文档总数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Elasticsearch索引写入统计';

CREATE TABLE IF NOT EXISTS t_elastic_thread_metric (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 主键ID
    cluster_code VARCHAR(255) NOT NULL, -- 集群代码
    node_name VARCHAR(255) NOT NULL, -- 节点名称
    thread_type VARCHAR(255) NOT NULL, -- 线程池类型
    active_threads INT DEFAULT 0, -- 活动线程数
    queue_size INT DEFAULT 0, -- 队列大小
    rejected_count BIGINT DEFAULT 0, -- 拒绝的任务数量
    completed_count BIGINT DEFAULT 0, -- 完成的任务数量
    largest_size INT DEFAULT 0, -- 历史最大线程数
    cpu_usage DOUBLE DEFAULT 0.00, -- CPU使用率（如果需要）
    memory_usage BIGINT DEFAULT 0, -- 内存使用情况（如果需要）
    collect_time DATETIME NOT NULL, -- 数据收集时间
    INDEX idx_cluster_node_type (cluster_code, node_name, thread_type), -- 复合索引，提高查询效率
    INDEX idx_collect_time (collect_time) -- 对collect_time建立索引，便于按时间范围查询
);

CREATE TABLE t_slow_query (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',

    -- 集群 & 节点信息
    cluster_code VARCHAR(64) NOT NULL COMMENT '集群编码（如 prod-es-cluster）',
    node_id VARCHAR(128) NOT NULL COMMENT '节点ID（Elasticsearch节点唯一标识）',
    node_name VARCHAR(128) COMMENT '节点名称（可选）',

    -- 查询上下文信息
    task_id VARCHAR(128) NOT NULL COMMENT '任务ID（对应Elasticsearch中的task_id）',
    action VARCHAR(64) COMMENT '任务类型（如 search:query, search:scroll）',
    query_type VARCHAR(32) COMMENT '查询类型（search, aggregation, scroll, suggest）',
    index_name VARCHAR(256) COMMENT '索引名称（可能为空，多个用逗号分隔）',

    -- 执行时间与状态
    start_time DATETIME NOT NULL COMMENT '任务开始时间',
    execution_time_ms BIGINT NOT NULL COMMENT '执行耗时（毫秒）',
    status VARCHAR(32) NOT NULL COMMENT '任务状态（running, completed, killed, failed）',

    -- 查询内容与上下文
    query_content TEXT COMMENT '查询语句摘要或完整DSL内容',
    description TEXT COMMENT '任务描述（如SearchSourceBuilder{...}）',
    stack_trace TEXT COMMENT '堆栈信息（用于异常情况下的调试）',

    -- 其他信息
    collect_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
    is_processed TINYINT DEFAULT 0 COMMENT '是否已处理（可用于标记是否已发送告警等）',

    -- 索引优化
    INDEX idx_cluster_task(cluster_code, task_id),
    INDEX idx_start_time(start_time),
    INDEX idx_execution_time(execution_time_ms)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Elasticsearch 慢查询日志记录表';


CREATE TABLE `t_slow_query_kill` (
  `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',

  `query_id` INT NULL DEFAULT NULL COMMENT '关联的查询ID',
  `index_name` VARCHAR(255) NULL DEFAULT NULL COMMENT '索引名称',
  `query_type` VARCHAR(100) NULL DEFAULT NULL COMMENT '查询类型',
  `kill_time` VARCHAR(30) NULL DEFAULT NULL COMMENT '终止时间（字符串格式）',
  `execution_time` BIGINT NULL DEFAULT NULL COMMENT '执行时间（毫秒）',
  `status` VARCHAR(50) NULL DEFAULT NULL COMMENT '状态（如killed）',
  `reason` text NULL DEFAULT NULL COMMENT '超时原因',
  `node_id` VARCHAR(255) NULL DEFAULT NULL COMMENT '节点ID',
  `task_id` VARCHAR(255) NULL DEFAULT NULL COMMENT '任务ID',
  `query_content` TEXT NULL DEFAULT NULL COMMENT '原始查询内容',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='慢查询终止记录表';

CREATE TABLE IF NOT EXISTS t_elastic_node_metric (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    cluster_code VARCHAR(64) NOT NULL COMMENT '集群编码',
    node_id VARCHAR(128) NOT NULL COMMENT '节点ID',
    node_name VARCHAR(128) NOT NULL COMMENT '节点名称',
    host_ip VARCHAR(64) COMMENT '主机IP',
    transport_address VARCHAR(128) COMMENT '传输地址',
    roles VARCHAR(255) COMMENT '节点角色',
    
    -- CPU指标
    cpu_percent INT COMMENT 'CPU使用率(%)',
    
    -- 内存指标
    os_mem_total DOUBLE COMMENT '系统总内存(GB)',
    os_mem_free DOUBLE COMMENT '系统空闲内存(GB)',
    os_mem_used DOUBLE COMMENT '系统已用内存(GB)',
    os_mem_used_percent INT COMMENT '系统内存使用率(%)',
    os_mem_free_percent INT COMMENT '系统内存空闲率(%)',
    jvm_mem_heap_used DOUBLE COMMENT 'JVM堆内存使用量(GB)',
    jvm_mem_heap_used_percent INT COMMENT 'JVM堆内存使用率(%)',
    jvm_mem_heap_max DOUBLE COMMENT 'JVM最大堆内存(GB)',
    
    -- 磁盘指标
    disk_total VARCHAR(64) COMMENT '磁盘总空间',
    disk_used VARCHAR(64) COMMENT '磁盘已用空间',
    disk_avail VARCHAR(64) COMMENT '磁盘可用空间',
    disk_percent DOUBLE COMMENT '磁盘使用率(%)',
    
    -- 文件描述符
    open_file_descriptors INT COMMENT '打开的文件描述符数',
    max_file_descriptors INT COMMENT '最大文件描述符数',
    
    -- 线程指标
    threads_count INT COMMENT '线程数',
    
    -- 网络指标
    network_rx_size BIGINT COMMENT '网络接收字节数',
    network_rx_packets BIGINT COMMENT '网络接收包数',
    network_tx_size BIGINT COMMENT '网络发送字节数',
    network_tx_packets BIGINT COMMENT '网络发送包数',
    
    -- IO指标
    io_read_operations BIGINT COMMENT 'IO读操作数',
    io_write_operations BIGINT COMMENT 'IO写操作数',
    io_read_size BIGINT COMMENT 'IO读取字节数',
    io_write_size BIGINT COMMENT 'IO写入字节数',
    
    -- 其他指标
    shards_count INT COMMENT '分片数量',
    indices_count INT COMMENT '索引数量',
    
    collect_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
    
    INDEX idx_cluster_node (cluster_code, node_name),
    INDEX idx_collect_time (collect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Elasticsearch节点指标监控表';


ALTER TABLE elastic_monitor.t_elastic_template ADD auto_create BOOL NULL;


CREATE TABLE `t_config` (
    `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',

    `module_name` VARCHAR(255) DEFAULT NULL COMMENT '模块名称',
    `entity_name` VARCHAR(255) DEFAULT NULL COMMENT '实体名称',
    `section_name` VARCHAR(255) DEFAULT NULL COMMENT '配置项所属节',

    `config_key` VARCHAR(255) DEFAULT NULL COMMENT '配置键名',
    `config_name` VARCHAR(256) NOT NULL COMMENT '配置名称（唯一标识）',
    `config_value` VARCHAR(256) DEFAULT NULL COMMENT '配置值',
    `config_desc` VARCHAR(256) DEFAULT '' COMMENT '配置描述（默认空字符串）',
    `config_content` VARCHAR(256) DEFAULT NULL COMMENT '配置内容',

    `create_time` VARCHAR(30) DEFAULT NULL COMMENT '创建时间（字符串格式）',
    `update_time` VARCHAR(30) DEFAULT NULL COMMENT '更新时间（字符串格式）',

    -- 索引建议
    INDEX idx_config_name (`config_name`),
    INDEX idx_config_key (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配置项表';

ALTER TABLE elastic_monitor.t_config ADD cluster_code varchar(100) NULL;

CREATE TABLE `t_disk_clear` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_name` VARCHAR(255) NOT NULL COMMENT '模板名称',
  `min_low_threshold` INT NOT NULL DEFAULT 80 COMMENT '磁盘清理最低阈值（百分比）',
  `priority` INT NOT NULL DEFAULT 5 COMMENT '清理优先级（1-10）',
  `retention_period` INT NOT NULL DEFAULT 7 COMMENT '保留周期（天）',
  `min_index_size` INT NOT NULL DEFAULT 1 COMMENT '最小保留索引数量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_template_name` (`template_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='磁盘清理模板配置表';
