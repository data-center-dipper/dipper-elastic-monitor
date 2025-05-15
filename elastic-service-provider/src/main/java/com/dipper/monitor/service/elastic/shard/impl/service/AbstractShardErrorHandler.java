package com.dipper.monitor.service.elastic.shard.impl.service;

import com.dipper.monitor.beans.SpringUtil;
import com.dipper.monitor.service.elastic.alians.ElasticAliansService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.disk.ElasticDiskService;
import com.dipper.monitor.service.elastic.life.ElasticRealLifecyclePoliciesService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.overview.ElasticHealthService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.nio.entity.NStringEntity;

import java.io.UnsupportedEncodingException;

@Slf4j
public abstract class AbstractShardErrorHandler {


    protected ElasticRealLifecyclePoliciesService elasticRealLifecyclePoliciesService;
    protected ElasticShardService elasticShardService;
    protected ElasticClientService elasticClientService;
    protected ElasticAliansService elasticAliansService;
    protected ElasticRealNodeService elasticRealNodeService;
    protected ElasticDiskService elasticDiskService;
    protected ElasticHealthService elasticHealthService;

    protected static final String HENGXIAN = "\r\n---------------------------------------------------------\r\n";

    protected StringBuilder builder = new StringBuilder(3000);

    public AbstractShardErrorHandler(){
        elasticRealLifecyclePoliciesService = SpringUtil.getBean(ElasticRealLifecyclePoliciesService.class);
        elasticShardService = SpringUtil.getBean(ElasticShardService.class);
        elasticClientService = SpringUtil.getBean(ElasticClientService.class);
        elasticAliansService = SpringUtil.getBean(ElasticAliansService.class);
        elasticRealNodeService = SpringUtil.getBean(ElasticRealNodeService.class);
        elasticDiskService = SpringUtil.getBean(ElasticDiskService.class);
        elasticHealthService = SpringUtil.getBean(ElasticHealthService.class);

        init();
    }

    public void init() {
    }


    protected void resetReplicas(StringBuilder builder, Integer nodeCount, Integer numReolicasInt, String index) throws UnsupportedEncodingException, UnsupportedEncodingException {
        String api = "/" + index + "/_settings";
        if (nodeCount.intValue() < numReolicasInt.intValue()) {
            numReolicasInt = Integer.valueOf(nodeCount.intValue() / 2);
            builder.append("本次将副本设置为节点数的一半:").append(nodeCount).append("\r\n");
        } else {
            numReolicasInt = Integer.valueOf(numReolicasInt.intValue() / 2);
            builder.append("本次将副本设置为当前的一半:").append(numReolicasInt).append("\r\n");
        }
        log.info("计算最终副本数大小：{}", numReolicasInt);
        builder.append("计算最终副本数大小:").append(numReolicasInt).append("\r\n");

        String body = "{\n  \"number_of_replicas\":" + numReolicasInt + "\n}\n";

        builder.append("PUT ").append(api).append("\r\n").append(body);
        NStringEntity nStringEntity = new NStringEntity(body);
        String result2 = elasticClientService.executePutApi(api, (HttpEntity)nStringEntity);
        builder.append("设置新的分片：").append("\r\n")
                .append(result2).append("\r\n");
        builder.append("修改完毕，请刷新，如果还是这个问题，请再次点击修复").append("\r\n");
    }
}
