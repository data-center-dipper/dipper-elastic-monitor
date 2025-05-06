package com.dipper.monitor.service.elastic.cluster.impl;

import cn.hutool.core.bean.BeanUtil;
import com.dipper.common.lib.utils.TelnetUtils;
import com.dipper.common.lib.utils.Tuple2;
import com.dipper.monitor.entity.db.elastic.ElasticClusterEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterReq;
import com.dipper.monitor.entity.elastic.cluster.ElasticClusterRegisterReq;
import com.dipper.monitor.entity.elastic.cluster.ElasticClusterView;
import com.dipper.monitor.listeners.publish.RefreshNodesEventPublisher;
import com.dipper.monitor.mapper.ElasticClusterManagerMapper;
import com.dipper.monitor.service.elastic.cluster.ElasticClusterManagerService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ElasticClusterManagerServiceImpl implements ElasticClusterManagerService {

    @Autowired
    private ElasticClusterManagerMapper elasticClusterManagerMapper;
    @Autowired
    private ElasticRealNodeService elasticRealNodeService;
    @Autowired
    private RefreshNodesEventPublisher refreshNodesEventPublisher;

    private final static Cache<String, CurrentClusterEntity> currentMap = CacheBuilder.newBuilder()
            .maximumSize(7)
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .concurrencyLevel(4)
            .build();
    private final static String current_cluster = "current_cluster";

    private void invalidCache() {
        currentMap.invalidateAll();
    }

    @Override
    @Transactional
    public void registerCluster(ElasticClusterRegisterReq clusterRegisterReq) {
        String clusterCode = clusterRegisterReq.getClusterCode();
        String clusterName = clusterRegisterReq.getClusterName();
        String address = clusterRegisterReq.getAddress();

        commonCheck(clusterRegisterReq);

        ElasticClusterEntity clusterByCode = elasticClusterManagerMapper.getClusterByCode(clusterCode);
        if (clusterByCode != null) {
            throw new IllegalArgumentException("集群唯一识别码对于的集群已经存在");
        }
        List<ElasticClusterEntity> allClusterList = elasticClusterManagerMapper.getAllClusterList();

        ElasticClusterEntity elasticClusterEntity = new ElasticClusterEntity();
        elasticClusterEntity.setClusterDesc(clusterRegisterReq.getClusterDesc());
        elasticClusterEntity.setClusterName(clusterName);
        elasticClusterEntity.setClusterCode(clusterCode);
        elasticClusterEntity.setAddress(address);
        if (allClusterList == null || allClusterList.isEmpty()) {
            elasticClusterEntity.setDefaultCluster(true);
            elasticClusterEntity.setCurrentCluster(true);
        }

        elasticClusterManagerMapper.save(elasticClusterEntity);

        // todo: 这里使用事件发布是因为 在同一个事务中，刚刚插入的无法立即查询到
        refreshNodesEventPublisher.publishCustomEvent("refresh nodes");

        invalidCache();
    }



    private void commonCheck(ElasticClusterRegisterReq clusterRegisterReq) {
        String clusterCode = clusterRegisterReq.getClusterCode();
        String clusterName = clusterRegisterReq.getClusterName();
        String address = clusterRegisterReq.getAddress();
        Boolean checkAddress = clusterRegisterReq.getCheckAddress();
        if (StringUtils.isBlank(clusterCode)) {
            throw new IllegalArgumentException("集群唯一识别码未填写");
        }
        if (StringUtils.isBlank(clusterName)) {
            throw new IllegalArgumentException("集群名称未填写");
        }
        if (StringUtils.isBlank(address)) {
            throw new IllegalArgumentException("集群地址未填写");
        }
        if (checkAddress) {
            // 判断地址是否通畅
            Tuple2<Boolean, String> allAddress = TelnetUtils.telnetAllAddress(address, 3000);
            Boolean good = allAddress.getK();
            String addressBad = allAddress.getV();
            if (!good) {
                throw new IllegalArgumentException("集群地址链接异常:" + addressBad);
            }
        }
    }

    @Override
    public void updateCluster(ElasticClusterRegisterReq clusterRegisterReq) {
        commonCheck(clusterRegisterReq);

        String clusterCode = clusterRegisterReq.getClusterCode();
        String clusterName = clusterRegisterReq.getClusterName();
        String address = clusterRegisterReq.getAddress();


        ElasticClusterEntity elasticClusterEntity = new ElasticClusterEntity();
        elasticClusterEntity.setClusterDesc(clusterRegisterReq.getClusterDesc());
        elasticClusterEntity.setClusterName(clusterName);
        elasticClusterEntity.setClusterCode(clusterCode);
        elasticClusterEntity.setAddress(address);

        elasticClusterManagerMapper.update(elasticClusterEntity);

        refreshNodesEventPublisher.publishCustomEvent("refresh nodes");

        invalidCache();
    }

    @Override
    public void deleteCluster(String clusterCode) {
        checkClusterExist(clusterCode);
        elasticClusterManagerMapper.deleteCluster(clusterCode);
        invalidCache();
    }

    private void checkClusterExist(String clusterCode) {
        if (StringUtils.isBlank(clusterCode)) {
            throw new IllegalArgumentException("集群唯一识别码未传参");
        }
        ElasticClusterEntity clusterByCode = elasticClusterManagerMapper.getClusterByCode(clusterCode);
        if (clusterByCode == null) {
            throw new IllegalArgumentException("集群唯一识别码对于的集群不存在");
        }
    }

    @Override
    public void setDefaultCluster(String clusterCode) {
        checkClusterExist(clusterCode);
        elasticClusterManagerMapper.clearDefaultCluster(clusterCode);
        elasticClusterManagerMapper.setDefaultCluster(clusterCode);
        invalidCache();
    }


    @Override
    public void setCurrentCluster(CurrentClusterReq currentClusterReq) {
        String clusterCode = currentClusterReq.getClusterCode();
        Boolean currentEnable = currentClusterReq.getCurrentEnable();
        checkClusterExist(clusterCode);
        elasticClusterManagerMapper.clearCurrentCluster(clusterCode);
        elasticClusterManagerMapper.setCurrentCluster(clusterCode,currentEnable);
        invalidCache();
    }

    @Override
    public CurrentClusterEntity getCurrentCluster() {
        CurrentClusterEntity currentCluster = currentMap.getIfPresent(current_cluster);
        if (currentCluster != null) {
            return currentCluster;
        }
        currentCluster = createCurrentCluster();
        if (currentCluster != null) {
            currentMap.put(current_cluster, currentCluster);
        }
        return currentCluster;
    }

    private CurrentClusterEntity createCurrentCluster() {
        ElasticClusterEntity currentCluster = elasticClusterManagerMapper.getCurrentCluster();
        if (currentCluster == null) {
            return null;
        }
        CurrentClusterEntity currentClusterEntity = new CurrentClusterEntity();
        BeanUtil.copyProperties(currentCluster, currentClusterEntity);

        return currentClusterEntity;
    }


    private String getZkSuffix(String zookeeperAddress) {
        // 获取zk地址后缀 地址如： localhost:2181,localhost:2182,localhost:2183/elastic 获取 /elastic
        if (StringUtils.isBlank(zookeeperAddress)) {
            return "";
        }
        String[] split = zookeeperAddress.split("/");
        if (split.length == 1) {
            return "";
        }
        return split[split.length - 1];
    }



    @Override
    public ElasticClusterEntity getCurrentClusterDetail(String clusterCode) {
        checkClusterExist(clusterCode);
        ElasticClusterEntity clusterByCode = elasticClusterManagerMapper.getClusterByCode(clusterCode);
        return clusterByCode;
    }

    @Override
    public List<ElasticClusterView> getAllCluster() {
        List<ElasticClusterEntity> allClusterList = elasticClusterManagerMapper.getAllClusterList();
        // 将 currentCluster = true 的排在第一位
        allClusterList.sort(Comparator.comparing(ElasticClusterEntity::getCurrentCluster).reversed());
        // 转换成 view 不使用流
        List<ElasticClusterView> allClusterViewList = new ArrayList<>();
        for (ElasticClusterEntity clusterEntity : allClusterList) {
            ElasticClusterView elasticClusterView = new ElasticClusterView();
            BeanUtils.copyProperties(clusterEntity, elasticClusterView);

            // 截断地址，超过 60个字符 截断成  xxx...
            String address =  getTruncationData(clusterEntity.getAddress(),40);
            elasticClusterView.setAddress(address);

            // 截断jmx地址，超过 60个字符 截断成  xxx...
            String jmxAddress =  getTruncationData(clusterEntity.getKafkaJmxAddress(),40);
            elasticClusterView.setKafkaJmxAddress(jmxAddress);

            // 截断zk地址，超过 60个字符 截断成  xxx...
            String zookeeperAddress =  getTruncationData(clusterEntity.getZookeeperAddress(),40);
            elasticClusterView.setZookeeperAddress(zookeeperAddress);

            // 截断描述，超过 200个字符 截断成  xxx...
            String clusterDesc =  getTruncationData(clusterEntity.getClusterDesc(),130);
            elasticClusterView.setClusterDesc(clusterDesc);

            allClusterViewList.add(elasticClusterView);
        }


        return allClusterViewList;
    }


    private String getTruncationData(String address, int length) {
        if(StringUtils.isBlank(address)){
            return address;
        }
        if (address.length() > length) {
            return address.substring(0, length) + "...";
        }
        return address;
    }

//    @Override
//    public void updateMonitoringPolicy(MonitoringPolicyUpdateDTO policy) {
//        String clusterCode = policy.getClusterCode();
//        KafkaClusterEntity clusterByCode = kafkaClusterManagerMapper.getClusterByCode(clusterCode);
//        if (clusterByCode == null) {
//            throw new IllegalArgumentException("唯一识别码对应的集群不存在");
//        }
//        MonitoringPolicy monitoringPolicy = MonitoringPolicy.fromValue(policy.getMonitoringPolicy());
//
//        if (MonitoringPolicy.NONE == monitoringPolicy) {
//            // 清除所有监控设置
//            kafkaClusterManagerMapper.clearMonitoringPolicy(policy.getClusterCode());
//        } else if (MonitoringPolicy.FROM_NOW == monitoringPolicy) {
//            // 设置监控策略为从现在开始
//            kafkaClusterManagerMapper.setMonitoringFromNow(policy.getClusterCode());
//        } else if (MonitoringPolicy.TIME_RANGE == monitoringPolicy) {
//            // 检查并设置时间范围
//            if (policy.getMonitorStartTime() == null || policy.getMonitorEndTime() == null) {
//                throw new IllegalArgumentException("Time range is required for 'range' policy");
//            }
//            kafkaClusterManagerMapper.setMonitoringTimeRange(policy);
//        } else {
//            throw new IllegalArgumentException("Unsupported monitoring policy");
//        }
//    }


}
