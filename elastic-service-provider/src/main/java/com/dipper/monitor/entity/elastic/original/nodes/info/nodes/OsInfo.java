package com.dipper.monitor.entity.elastic.original.nodes.info.nodes;

import lombok.Data;

@Data
public class OsInfo {
    private String prettyName;
    private String arch;
    private String availableProcessors;
    private String allocatedProcessors;
}
