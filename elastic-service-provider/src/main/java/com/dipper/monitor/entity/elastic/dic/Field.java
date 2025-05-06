package com.dipper.monitor.entity.elastic.dic;

import lombok.Data;

@Data
public class Field {
    private Integer id;
    private String zhName;
    private String enName;
    private String fieldType;
    private String esMappingType;
    private Integer dicId;
}