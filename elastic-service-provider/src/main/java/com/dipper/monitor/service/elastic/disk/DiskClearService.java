package com.dipper.monitor.service.elastic.disk;

import com.dipper.monitor.entity.elastic.disk.GlobalDiskClearReq;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;

import java.io.IOException;
import java.util.List;

public interface DiskClearService {
    /**
     * 全局清理设置
     */
    void globalDiskClear(GlobalDiskClearReq globalDiskClearReq);

    /**
     * 节点磁盘负载top10
     * @return
     */
    List<ElasticNodeDisk> nodeDiskTop10() throws IOException;
}
