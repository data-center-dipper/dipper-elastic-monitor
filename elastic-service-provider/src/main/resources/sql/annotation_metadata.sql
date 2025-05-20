CREATE TABLE IF NOT EXISTS `t_annotation_metadata` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `annotation_type` varchar(255) NOT NULL COMMENT '注解类型名称',
  `class_name` varchar(255) NOT NULL COMMENT '类名',
  `method_name` varchar(255) NOT NULL COMMENT '方法名',
  `cron` varchar(100) DEFAULT NULL COMMENT 'cron表达式',
  `fixed_rate` bigint(20) DEFAULT NULL COMMENT '固定速率',
  `fixed_delay` bigint(20) DEFAULT NULL COMMENT '固定延迟',
  `author` varchar(100) DEFAULT NULL COMMENT '作者',
  `group_name` varchar(100) DEFAULT NULL COMMENT '组名',
  `job_desc` varchar(255) DEFAULT NULL COMMENT '任务描述',
  `edit_able` tinyint(1) DEFAULT '0' COMMENT '是否可编辑',
  `additional_attributes` text COMMENT '其他属性（JSON格式）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_annotation_type` (`annotation_type`),
  KEY `idx_class_method` (`class_name`,`method_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='注解元数据表';