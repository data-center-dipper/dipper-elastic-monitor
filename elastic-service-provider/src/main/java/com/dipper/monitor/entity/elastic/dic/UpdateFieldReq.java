package com.dipper.monitor.entity.elastic.dic;

import lombok.Data;

@Data
public class UpdateFieldReq {
    private Integer id;
    private String zhName; // 中文名称
    private String enName; // 英文名称
    private String businessAttribute; // 业务属性
    private String fieldType; // 字段类型
    private String esType; // ES类型
    private String dicName; // 所属字典名称
}
