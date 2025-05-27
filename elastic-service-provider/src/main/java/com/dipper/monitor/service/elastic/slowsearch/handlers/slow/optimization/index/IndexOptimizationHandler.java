package com.dipper.monitor.service.elastic.slowsearch.handlers.slow.optimization.index;

import com.dipper.monitor.entity.elastic.slowsearch.slow.QueryOptimizationReq;
import com.dipper.monitor.service.elastic.client.ElasticClientService;

public class IndexOptimizationHandler {
    private ElasticClientService elasticClientService;

    public IndexOptimizationHandler(ElasticClientService elasticClientService) {
        this.elasticClientService = elasticClientService;
    }

    public String indexOptimization(QueryOptimizationReq queryOptimizationReq) {

        return null;
    }
}
