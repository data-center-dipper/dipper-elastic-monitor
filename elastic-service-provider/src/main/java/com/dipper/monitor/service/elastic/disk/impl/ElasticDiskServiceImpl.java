package com.dipper.monitor.service.elastic.disk.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.dipper.monitor.entity.elastic.disk.DiskAllocationInfo;
import com.dipper.monitor.entity.elastic.disk.DiskWatermarkInfo;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.disk.ElasticDiskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class ElasticDiskServiceImpl implements ElasticDiskService {

    @Autowired
    private ElasticClientService elasticClientService;

    @Override
    public DiskWatermarkInfo getDiskWatermark() throws IOException {
        String clusterSetting = elasticClientService.executeGetApi(ElasticRestApi.CLUSTER_SETTING.getApiPath());
        JSONObject clusteringSettingJson = JSON.parseObject(clusterSetting);
        String enable = (String) JSONPath.eval(clusteringSettingJson, "$.transient.cluster.routing.allocation.enable");
        String diskThreshouldEnabled = (String)JSONPath.eval(clusteringSettingJson, "$.transient.cluster.routing.allocation.disk.threshould_enabled");
        String updateInterval = (String)JSONPath.eval(clusteringSettingJson, "$.transient.cluster.info.update.interval");


        String highDisk = (String)JSONPath.eval(clusteringSettingJson, "$.transient.cluster.routing.allocation.disk.watermark.high");
        String lowDisk = (String)JSONPath.eval(clusteringSettingJson, "$.transient.cluster.routing.allocation.disk.watermark.low");
        if (StringUtils.isBlank(highDisk)) {
            highDisk = "85%";
        }
        int highDiskNumber = 0;
        if (highDisk.contains("%")) {
            highDiskNumber = (int)Math.round(Double.valueOf(highDisk.replace("%", "")).doubleValue());
        }
        if (highDisk.contains(".")) {
            highDiskNumber = (int)Math.round(Double.valueOf(highDisk).doubleValue() * 100.0D);
        }

        DiskWatermarkInfo diskWatermarkInfo = new DiskWatermarkInfo();
        diskWatermarkInfo.setRoutingAllocationEnable(enable);
        diskWatermarkInfo.setDiskThreshouldEnabled(diskThreshouldEnabled);
        diskWatermarkInfo.setUpdateInterval(updateInterval);
        diskWatermarkInfo.setHighDiskNumber(highDiskNumber);
        diskWatermarkInfo.setLowDisk(lowDisk);

        return diskWatermarkInfo;
    }

    @Override
    public List<DiskAllocationInfo> getDiskAllocation() {
        List<DiskAllocationInfo> diskAllocations = new ArrayList<>();
        JSONArray indexDiskJson = null;
        try {
            String indexDisk = elasticClientService.executeGetApi(ElasticRestApi.INDEX_DISK_MESSAGE_JSON.getApiPath());
            indexDiskJson = JSON.parseArray(indexDisk);
        } catch (Exception e) {
            log.error("获取磁盘信息出错：{}", e.getMessage(), e);
            return Collections.emptyList();
        }
        if (indexDiskJson != null) {
            for (Iterator<Object> nodeItera = indexDiskJson.iterator(); nodeItera.hasNext(); ) {
                JSONObject obj = (JSONObject)nodeItera.next();
                DiskAllocationInfo info = new DiskAllocationInfo();
                info.setShards(obj.getString("shards"));
                info.setDiskIndices(obj.getString("disk.indices"));
                info.setDiskUsed(obj.getString("disk.used"));
                info.setDiskAvail(obj.getString("disk.avail"));
                info.setDiskTotal(obj.getString("disk.total"));
                info.setDiskPercent(obj.getString("disk.percent"));
                info.setHost(obj.getString("host"));
                info.setIp(obj.getString("ip"));
                info.setNode(obj.getString("node"));
                // 添加到列表
                diskAllocations.add(info);
            }
        }
        return diskAllocations;
    }
}
