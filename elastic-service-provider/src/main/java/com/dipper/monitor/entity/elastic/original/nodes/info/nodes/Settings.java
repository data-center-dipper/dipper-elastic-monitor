package com.dipper.monitor.entity.elastic.original.nodes.info.nodes;

import lombok.Data;

import java.util.List;

@Data
public class Settings {
    private String clusterName;
    private List<String> initialMasterNodes;
    private ThreadPool threadPool;
}
