package com.dipper.monitor.service.elastic.overview.impl.service;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.service.elastic.life.LifecyclePoliciesService;
import com.dipper.monitor.service.elastic.shard.ShardService;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

@Slf4j
public class ClusterErrorService {

    private   LifecyclePoliciesService lifecyclePoliciesService ;
    private   ShardService shardService ;

    public ClusterErrorService(){
        lifecyclePoliciesService = SpringUtil.getBean(LifecyclePoliciesService.class);
        shardService = SpringUtil.getBean(ShardService.class);
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
            List<JSONObject> shardError = shardService.getShardError();
            return shardError.size();
        }catch (Exception e){
            log.error("获取分片异常",e);
        }
        return 0;
    }
}
