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
