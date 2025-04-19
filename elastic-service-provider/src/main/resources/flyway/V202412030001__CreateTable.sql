
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
