package com.dipper.monitor.service.elastic.disk.impl;

import com.dipper.monitor.entity.db.config.ConfItemEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.disk.GlobalDiskClearReq;
import com.dipper.monitor.entity.elastic.nodes.risk.ElasticNodeDisk;
import com.dipper.monitor.enums.props.PropsEnum;
import com.dipper.monitor.service.config.PropsService;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.disk.DiskClearService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

@Service
@Slf4j
public class DiskClearServiceImpl implements DiskClearService {

    @Autowired
    private PropsService propsService;
    @Autowired
    private ElasticRealNodeService elasticRealNodeService;

    @Override
    public void globalDiskClear(GlobalDiskClearReq globalDiskClearReq) {

        ConfItemEntity item = new ConfItemEntity(PropsEnum.ES_DISK_CLEAR_LOW);
        item.setConfigValue(globalDiskClearReq.getLowThreshold() + "");
        propsService.saveOrUpdate(item);

        ConfItemEntity itemMiddle = new ConfItemEntity(PropsEnum.ES_DISK_CLEAR_MIDDLE);
        itemMiddle.setConfigValue(globalDiskClearReq.getMediumThreshold() + "");
        propsService.saveOrUpdate(itemMiddle);

        ConfItemEntity itemHigh = new ConfItemEntity(PropsEnum.ES_DISK_CLEAR_HIGH);
        itemHigh.setConfigValue(globalDiskClearReq.getHighThreshold() + "");
        propsService.saveOrUpdate(itemHigh);
    }

    @Override
    public List<ElasticNodeDisk> nodeDiskTop10() throws IOException {
        List<ElasticNodeDisk> elasticNodeDisks = elasticRealNodeService.nodeDiskTop10();
        return elasticNodeDisks;
    }


}
