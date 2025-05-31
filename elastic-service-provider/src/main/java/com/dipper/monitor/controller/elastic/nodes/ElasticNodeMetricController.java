package com.dipper.monitor.controller.elastic.nodes;

import com.dipper.monitor.entity.db.elastic.ElasticNodeMetricEntity;
import com.dipper.monitor.entity.elastic.nodes.metric.NodeMetricHistoryReq;
import com.dipper.monitor.entity.elastic.nodes.metric.NodeMetricHistoryView;
import com.dipper.monitor.mapper.NodeMetricStoreMapper;
import com.dipper.monitor.service.elastic.nodes.ElasticNodeStoreService;
import com.dipper.monitor.service.elastic.nodes.NodeMetricStoreService;
import com.dipper.monitor.utils.ResultUtils;
import com.dipper.monitor.utils.elastic.ElasticBeanUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dipper/monitor/api/v1/elastic/node_metric")
@Tag(name = "Elastic节点管理")
public class ElasticNodeMetricController {
    
    @Autowired
    private NodeMetricStoreService nodeMetricStoreService;
    @Autowired
    private ElasticNodeStoreService elasticNodeStoreService;

    /**
     * 获取所有节点的最新监控数据
     */
    /**
     * 获取节点列表（支持模糊搜索）
     */
    @GetMapping("/metricNodes")
    @Operation(summary = "获取节点列表（支持模糊搜索）")
    public Map<String, Object> metricNodes(@RequestParam(required = false) String nodeNameLike) {
        try {
            List<String> nodeList = elasticNodeStoreService.metricNodes(nodeNameLike);
            
            // 解析节点信息并构建前端需要的格式
            List<Map<String, Object>> result = new ArrayList<>();
            for (String nodeInfo : nodeList) {
                String[] parts = nodeInfo.split(":");
                if (parts.length >= 3) {
                    Map<String, Object> node = new HashMap<>();
                    node.put("id", parts[0]);
                    node.put("name", parts[1]);
                    node.put("ip", parts[2]);
                    result.add(node);
                }
            }
            
            return ResultUtils.onSuccess(result);
        } catch (Exception e) {
            log.error("获取节点列表失败", e);
            return ResultUtils.onFail(e.getMessage());
        }
    }

    
    /**
     * 获取指定节点的历史监控数据
     */
    @PostMapping("/getNodeMetricHistory")
    @Operation(summary = "获取指定节点的历史监控数据")
    public Map<String, Object> getNodeMetricHistory(@RequestBody NodeMetricHistoryReq nodeMetricHistoryReq) {
        try {
            // 根据nodeId查找对应的nodeName
            NodeMetricHistoryView allNodes = nodeMetricStoreService.getNodeMetricHistory(nodeMetricHistoryReq);
            return ResultUtils.onSuccess(allNodes);
        } catch (Exception e) {
            log.error("获取节点历史监控数据失败", e);
           return ResultUtils.onFail(e.getMessage());
        }
    }
}
