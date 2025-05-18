package com.dipper.monitor.entity.elastic.alians;

import lombok.Data;

import java.util.List;

@Data
public class AliasRepairInfo {
    private String alias;
    private String keepIndex;
    private List<String> indexesToDisable;
}
