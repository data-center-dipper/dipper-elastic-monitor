package com.dipper.monitor.service.elastic.setting;

import com.dipper.monitor.entity.elastic.shard.recovery.AllocationEnableReq;

public interface ClusterSettingService {
    void enableOrCloseShardAllocation(AllocationEnableReq allocationEnableReq);

    String getShardAllocation();

    /**
     * 下线某个节点
     * @param nodeName
     */
    void setNodeOffline(String nodeName);

    /**
     * 取消下线某个节点
     * @param nodeName
     */
    void cancelNodeOffline(String nodeName);
}
