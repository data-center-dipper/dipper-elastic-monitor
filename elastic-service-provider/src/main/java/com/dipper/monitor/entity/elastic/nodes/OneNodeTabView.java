package com.dipper.monitor.entity.elastic.nodes;


import com.dipper.monitor.entity.elastic.nodes.list.JvmInfoView;
import com.dipper.monitor.entity.elastic.nodes.list.OsInfoView;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.entity.elastic.original.nodes.info.nodes.Settings;
import lombok.Data;

import java.util.List;

@Data
public class OneNodeTabView {
    private Integer nodeId;
    private String name;
    private String hostName;
    private Integer hostPort;
    private String address;
    private String version;
    private Long totalIndexingBuffer;
    private List<String> roles;
    private Settings settings;
    private boolean master;
    // 节点是否在线
    private String status;
    // 是否能telnet 通畅
    private String telnet;

    private JvmInfoView jvmInfoView;
    private OsInfoView osInfoView;
    private ElasticNodeDisk elasticNodeDisk;

}
