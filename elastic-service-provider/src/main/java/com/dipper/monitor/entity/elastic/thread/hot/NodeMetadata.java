package com.dipper.monitor.entity.elastic.thread.hot;

import lombok.Data;

@Data
public class NodeMetadata {
    private String nodeIdShort;     // 如 "1.es3"
    private String nodeId;          // 节点ID，如 "tEq2FE4oTwmBptZ5FYQm8w"
    private String nodeGuid;        // GUID，如 "eEgDpFa4SOiyYuyJKuTbvg"
    private String ip;              // IP地址
    private String hostAndPort;     // 主机和端口
    private String roles;           // 角色
    private String attributes;      // 属性列表
}