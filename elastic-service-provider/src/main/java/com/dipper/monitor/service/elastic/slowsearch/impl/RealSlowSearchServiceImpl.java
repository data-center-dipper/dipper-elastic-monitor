package com.dipper.monitor.service.elastic.slowsearch.impl;

import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.config.SlowSearchConfig;
import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.cluster.CurrentClusterEntity;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryTaskEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.slowsearch.RealSlowSearchService;
import com.dipper.monitor.service.elastic.slowsearch.handlers.SlowQueryParseHandler;
import com.dipper.monitor.service.elastic.slowsearch.handlers.TaskSlowQueryParseHandler;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import com.dipper.monitor.utils.mock.MockAllData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class RealSlowSearchServiceImpl implements RealSlowSearchService {

    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private SlowSearchConfig slowSearchConfig;

    // ES 慢查询API地址
    private static final String SLOW_SEARCH_API = "/_nodes/stats/indices/search";
    /**
     * 慢查询实时查看
     */
    private static final String REAL_SLOW_SEARCH_API = "/_tasks?actions=*search&detailed";


    @Override
    public List<SlowQueryEntity> getRelaNodesSlowQuery() throws IOException {
        String response = elasticClientService.executeGetApi(REAL_SLOW_SEARCH_API);

        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        int slowQueryThreshold = slowSearchConfig.getSlowQueryThreshold();

        com.dipper.monitor.service.elastic.slowsearch.handlers.SlowQueryParseHandler parseHandler = new SlowQueryParseHandler();
        List<SlowQueryEntity> slowQueries = parseHandler.parseSlowQueryResponse(response, clusterCode, slowQueryThreshold);

        return slowQueries;
    }


    @Override
    public List<SlowQueryTaskEntity> getRelaSlowQuery() throws IOException {
        if(ApplicationUtils.isWindows()){
            return MockAllData.getRelaSlowQuery();
        }
        String response = elasticClientService.executeGetApi(REAL_SLOW_SEARCH_API);

        CurrentClusterEntity currentCluster = ElasticBeanUtils.getCurrentCluster();
        String clusterCode = currentCluster.getClusterCode();
        int slowQueryThreshold = slowSearchConfig.getSlowQueryThreshold();

        TaskSlowQueryParseHandler parseHandler = new TaskSlowQueryParseHandler();
        List<SlowQueryTaskEntity> slowQueries = parseHandler.parseSlowQueryResponse(response, clusterCode, slowQueryThreshold);

        return slowQueries;
    }

    @Override
    public List<SlowQueryEntity> transToSlowQueryEntity(List<SlowQueryTaskEntity> taskSlowQueries) {
        if (taskSlowQueries == null || taskSlowQueries.isEmpty()) {
            return Collections.emptyList();
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();

        return taskSlowQueries.stream().map(task -> {
            SlowQueryEntity entity = new SlowQueryEntity();

            entity.setClusterCode(task.getClusterCode());
            entity.setNodeId(task.getNodeId());
            entity.setNodeName(task.getNodeName());
            entity.setTaskId(task.getTaskId());
            entity.setAction(task.getAction());
            entity.setQueryType(task.getQueryType());

            // 处理索引名称（假设 SlowQueryTaskEntity 没有 index_name 字段）
            // 如果需要可以从 description 提取或留空
            // 这里设为空字符串作为默认值
            entity.setIndexName("");

            entity.setDescription(task.getDescription());
            entity.setQueryContent(task.getDescription()); // 可选：queryContent 来自 description

            // 时间字段转换
            try {
                entity.setStartTime(task.getStartTime() != null ? inputFormat.parse(task.getStartTime()) : now);
            } catch (Exception e) {
                entity.setStartTime(now); // 解析失败时使用当前时间
            }

            entity.setExecutionTimeMs(task.getExecutionTime());
            entity.setStatus(task.getStatus());
            entity.setCollectTime(task.getCollectTime() != null ? task.getCollectTime() : now);

            // 默认未处理
            entity.setIsProcessed(0);

            // stackTrace 在 TaskEntity 中没有，设置为 null
            entity.setStackTrace(null);

            return entity;
        }).toList();
    }
}
