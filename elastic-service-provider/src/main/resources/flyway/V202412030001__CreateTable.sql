
CREATE TABLE `t_elastic_cluster` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `cluster_code` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '集群代码',
  `cluster_name` varchar(255) COLLATE utf8_bin NOT NULL COMMENT '集群名称',
  `address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '集群地址',
  `current_cluster` tinyint(1) DEFAULT '0' COMMENT '是否为当前使用的集群',
  `default_cluster` tinyint(1) DEFAULT '0' COMMENT '是否为默认集群',
  `cluster_desc` text COLLATE utf8_bin COMMENT '集群描述',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `cluster_code` (`cluster_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='elastic集群配置表';

CREATE TABLE t_elastic_node_store (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cluster_code VARCHAR(255) NOT NULL,
    host_name VARCHAR(255),
    host_ip VARCHAR(255),
    host_port INT,
    address TEXT,
    UNIQUE KEY unique_host (host_name, host_ip, host_port)
);

CREATE TABLE t_dic (
    id INT AUTO_INCREMENT PRIMARY KEY,
    zh_name VARCHAR(255) NOT NULL COMMENT '字典中文名称',
    en_name VARCHAR(255) NOT NULL COMMENT '字典英文名称',
    business_attribute TEXT COMMENT '业务属性'
) COMMENT='字典表';

CREATE TABLE t_field (
    id INT AUTO_INCREMENT PRIMARY KEY,
    field_type VARCHAR(255) NOT NULL COMMENT '字段类型',
    zh_name VARCHAR(255) NOT NULL COMMENT '字段中文名称',
    en_name VARCHAR(255) NOT NULL COMMENT '字段英文名称',
    es_mapping_type VARCHAR(255) NOT NULL COMMENT '映射到ES字典的类型',
    dic_id INT
) COMMENT='字段表';

CREATE TABLE t_elastic_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- 主键ID
    cluster_code VARCHAR(255) NOT NULL, -- 集群代码
    zh_name VARCHAR(255) NOT NULL, -- 中文名称
    en_name VARCHAR(255) NOT NULL, -- 英文名称
    settings TEXT, -- 设置信息(JSON格式)
    dic_name VARCHAR(255), -- 使用的字典名称（英文）
    index_patterns  VARCHAR(500) , -- 索引匹配模式(JSON或逗号分隔字符串)
    alians_patterns  VARCHAR(500), -- 别名匹配模式(JSON或逗号分隔字符串)
    number_of_shards INT, -- 分片数
    number_of_replicas INT, -- 副本数
    enable_auto_shards BOOLEAN DEFAULT FALSE, -- 是否开启自动 shard 计算
    template_content TEXT NOT NULL, -- 模板内容(JSON)
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- 创建时间，默认当前时间
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- 更新时间，默认当前时间，并随更新自动更改
    UNIQUE KEY unique_cluster_code_en_name (cluster_code, en_name) -- 唯一性约束，确保组合(cluster_code, zh_name, en_name)唯一
);

CREATE TABLE t_module_task_map (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    `cluster_code` varchar(191) COLLATE utf8mb4_bin NOT NULL,
    module_name VARCHAR(191) NOT NULL,
    entity_name VARCHAR(255) NOT NULL,
    `section_name` varchar(256) COLLATE utf8mb4_bin NOT NULL COMMENT '三级区分',
    task_code VARCHAR(191) NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
