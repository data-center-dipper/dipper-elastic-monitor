package com.dipper.monitor.entity.elastic.nodes.yaunshi;


import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.JvmInfo;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.OsInfo;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.PathInfo;
import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.Settings;
import lombok.Data;

import java.util.List;

@Data
public class EsNodeInfo {
    private String name;
    private String ip;
    private String host;
    private String version;
    private Long totalIndexingBuffer;
    private List<String> roles;
    private Settings settings;
    // JVM Info
    private JvmInfo jvmInfo;

    // OS Info
    private OsInfo osInfo ;

    // Path Info
    private PathInfo pathInfo;

    private boolean master;
}