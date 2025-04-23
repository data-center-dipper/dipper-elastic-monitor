package com.dipper.monitor.service.elastic.overview.impl.service;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.service.elastic.life.LifecyclePoliciesService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

@Slf4j
public class ClusterErrorService {

    private   LifecyclePoliciesService lifecyclePoliciesService ;
    private ElasticShardService elasticShardService;

    public ClusterErrorService(){
        lifecyclePoliciesService = SpringUtil.getBean(LifecyclePoliciesService.class);
        elasticShardService = SpringUtil.getBean(ElasticShardService.class);
    }

    public String getClusterError() {

        // 获取集群 生命周期是否存在异常
        Integer lifeError = getLifeError();
        // 获取集群 分片是否存在异常
        Integer shardError = getShardError();

        StringBuilder sb = new StringBuilder();
        if(lifeError > 0){
            sb.append("集群生命周期存在异常，请及时处理，异常数量：").append(lifeError).append("\n");
        }
        if(shardError > 0){
            sb.append("集群分片存在异常，请及时处理，异常数量：").append(shardError).append("\n");
        }
        return sb.toString();
    }

    private Integer getLifeError() {
        try {
//            Map<String, Object> lifeCycleList = lifecyclePoliciesService.getLifeCycleList();
//            return lifeCycleList.size();
        } catch (Exception e) {
            log.error("检查ILM问题时发生错误", e);
        }
        return 0;
    }

    private Integer getShardError() {
        try {
            List<JSONObject> shardError = elasticShardService.getShardError();
            return shardError.size();
        }catch (Exception e){
            log.error("获取分片异常",e);
        }
        return 0;
    }
}
