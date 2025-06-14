package com.dipper.monitor.service.elastic.data.impl;

import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.data.DataProcessService;
import com.dipper.monitor.service.elastic.data.ofline.NodeOfflineHandler;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.service.elastic.setting.ClusterSettingService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class DataProcessServiceImpl implements DataProcessService {

    private volatile NodeOfflineHandler nodeOfflineHandler ;
    @Autowired
    private ElasticRealNodeService elasticRealNodeService;
    @Autowired
    private ElasticHealthService elasticHealthService;
    @Autowired
    private ElasticNodeStoreService elasticNodeStoreService;
    @Autowired
    private ElasticShardService elasticShardService;
    @Autowired
    private ClusterSettingService clusterSettingService;
    @Autowired
    private ElasticClientService elasticClientService;

    @Override
    public void nodeOfflineApi(String nodeName) throws IOException {
        if(nodeOfflineHandler != null){
            throw new IllegalArgumentException("节点下线任务正在执行中");
        }
        NodeOfflineHandler nodeOfflineHandlerTemp = new NodeOfflineHandler(elasticRealNodeService, elasticHealthService,
                elasticNodeStoreService, elasticShardService, clusterSettingService, elasticClientService,
                nodeName);
        nodeOfflineHandlerTemp.check(nodeName);
        nodeOfflineHandlerTemp.start();

        // 最后再进行赋值
        nodeOfflineHandler = nodeOfflineHandlerTemp;
    }

    @Override
    public String nodeOfflineState() {
        String result = "任务迁移完成获不存在";
        if(nodeOfflineHandler == null){
            result = nodeOfflineHandler.getRecoveryState();
        }
        if (nodeOfflineHandler.isDown()) {
            log.info("节点下线任务已完成");
            nodeOfflineHandler = null;
        }
        return result;
    }
}
