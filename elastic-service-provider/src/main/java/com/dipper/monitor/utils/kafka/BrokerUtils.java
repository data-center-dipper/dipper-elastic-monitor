package com.dipper.monitor.utils.kafka;

import com.dipper.monitor.entity.elastic.cluster.NodeEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class BrokerUtils {
    public static List<NodeEntity> getBrokerHostAndPort(String brokerNames) {
        List<NodeEntity> brokerNameList = new ArrayList<>();
        if(StringUtils.isNotBlank(brokerNames)){
            String[] nodeItem = brokerNames.split(",");
            for (String item: nodeItem){
                String[] nodeAndPort = item.split(":");
                if(nodeAndPort.length <= 1){
                    continue;
                }
                String host = nodeAndPort[0];
                String port = nodeAndPort[1];
                NodeEntity nodeEntity = new NodeEntity();
                nodeEntity.setHost(host);
                nodeEntity.setPort(Integer.parseInt(port));
                brokerNameList.add(nodeEntity);
            }
        }
        return brokerNameList;
    }
}
