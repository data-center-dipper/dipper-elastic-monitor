package com.dipper.monitor.service.elastic.disk;

import com.dipper.monitor.entity.elastic.disk.clear.DiskClearItemReq;
import com.dipper.monitor.entity.elastic.disk.clear.DiskClearPageReq;
import com.dipper.monitor.entity.elastic.disk.clear.DiskClearView;
import com.dipper.monitor.entity.elastic.disk.GlobalDiskClearReq;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.utils.Tuple2;

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

    /**
     * 模板磁盘清理分页查询
     * @param diskClearPageReq
     * @return
     */
    Tuple2<Integer, List<DiskClearView>> templateDiskClearPage(DiskClearPageReq diskClearPageReq);

    /**
     * 模板磁盘清理设置
     * @param diskClearItemReq
     */
    void templateDiskSaveOrUpdate(DiskClearItemReq diskClearItemReq);

    /**
     * 模板磁盘清理删除
     * @param id
     */
    void templateDiskDelete(Integer id);
}
