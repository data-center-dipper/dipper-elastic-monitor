package com.dipper.monitor.entity.elastic.nodes.detail;

import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.entity.elastic.original.nodes.info.nodes.OsInfo;
import com.dipper.monitor.entity.elastic.original.nodes.info.nodes.PathInfo;
import lombok.Data;

/**
 * 节点详情响应实体类
 */
@Data
public class NodeDetailView {
    private Integer id;
    private String clusterCode;
    private String hostName;
    private String hostIp;
    private Integer hostPort;
    private String address;

    // JVM Info
    private JvmDetailView jvmInfoView;

    // OS Info
    private OsDetailView osDetailView ;

    private ElasticNodeDisk elasticNodeDisk;

    // Path Info
    private PathInfo pathInfo;

    private boolean master;
}