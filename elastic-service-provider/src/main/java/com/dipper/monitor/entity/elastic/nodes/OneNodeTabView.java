package com.dipper.monitor.entity.elastic.nodes;


import com.dipper.monitor.entity.elastic.nodes.yaunshi.nodes.Settings;
import lombok.Data;

import java.util.List;

@Data
public class OneNodeTabView {
    private String name;
    private String hostName;
    private Integer hostPort;
    private String address;
    private String version;
    private Long totalIndexingBuffer;
    private List<String> roles;
    private Settings settings;
    private JvmInfoView jvmInfoView;
    private boolean master;
    // 节点是否在线
    private String status;
    // 是否能telnet 通畅
    private String telnet;
}
