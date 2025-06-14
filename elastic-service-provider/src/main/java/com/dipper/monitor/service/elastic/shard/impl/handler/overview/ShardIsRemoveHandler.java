package com.dipper.monitor.service.elastic.shard.impl.handler.overview;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.shard.ShardEntity;
import com.dipper.monitor.entity.elastic.shard.overview.ShardRemoveView;
import com.dipper.monitor.entity.elastic.shard.recovery.RecoveryShardInfo;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import com.dipper.monitor.service.elastic.shard.ElasticShardService;
import com.dipper.monitor.service.elastic.shard.impl.handler.AbstractShardHandler;
import com.dipper.monitor.utils.Tuple2;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ShardIsRemoveHandler extends AbstractShardHandler {

    private final ElasticClientService elasticClientService;
    private final ElasticRealNodeService elasticRealNodeService;
    private final ElasticShardService elasticShardService;

    public ShardIsRemoveHandler(ElasticShardService elasticShardService,
                                ElasticClientService elasticClientService,
                                ElasticRealNodeService elasticRealNodeService) {
        this.elasticShardService = elasticShardService;
        this.elasticClientService = elasticClientService;
        this.elasticRealNodeService = elasticRealNodeService;
    }

    /**
     * 获取当前正在迁移中的分片，并返回分页后的视图数据
     */
    public Tuple2<Integer, List<ShardRemoveView>> shardIsRemove(PageReq pageReq) throws IOException {
        // 1. 获取所有分片信息
        List<RecoveryShardInfo> recoveryShardInfos = getShardsFromRecoveryApi();
        if (recoveryShardInfos == null || recoveryShardInfos.isEmpty()) {
            return new Tuple2<>(0, Collections.emptyList());
        }

        // 对这个map进行分页处理
        Tuple2<Integer, List<RecoveryShardInfo>> tuple2 = getDataPage(recoveryShardInfos, pageReq);
        Integer total = tuple2.getK();
        List<RecoveryShardInfo> pageShards = tuple2.getV();

        List<ShardRemoveView> pagedList = transToView(pageShards);

        return new Tuple2<>(total, pagedList);
    }

    public List<ShardRemoveView> transToView(List<RecoveryShardInfo> pageShards) {

        List<ShardRemoveView> result = new ArrayList<>();

        for (RecoveryShardInfo info : pageShards) {
            String index = info.getIndex();
            String shardId = info.getShard();

            result.add(new ShardRemoveView()
                    .setIndexName(info.getIndex())
                    .setShardId(info.getShard())
                    .setShardState("RELOCATING")
                    .setSourceNode(info.getSourceNode())
                    .setTargetNode(info.getTargetNode())
                    .setTime(info.getTime())
                    .setType(info.getType())
                    .setStage(info.getStage())
                    .setFilesPercent(info.getFilesPercent())
                    .setBytesPercent(info.getBytesPercent())
                    .setTranslogOpsPercent(info.getTranslogOpsPercent()));
        }

        return result;
    }

    /**
     * 调用 Elasticsearch 的 _cat/recovery 接口，获取正在迁移的分片信息，并封装成 RecoveryShardInfo 列表
     */
    public List<RecoveryShardInfo> getShardsFromRecoveryApi() throws IOException {
        String api = "/_cat/recovery?format=json";
        String responseJson = elasticClientService.executeGetApi(api);
        JSONArray recoveryList = JSONObject.parseArray(responseJson);

        List<RecoveryShardInfo> result = new ArrayList<>();

        for (Object obj : recoveryList) {
            JSONObject recovery = (JSONObject) obj;

            // 只处理正在进行中的迁移（排除 stage=done）
//            if ("done".equalsIgnoreCase(recovery.getString("stage"))) {
//                continue;
//            }

            // source_node 为 n/a 表示不是迁移任务（可能是初始化等）
            String sourceNode = recovery.getString("source_node");
            if ("n/a".equals(sourceNode)) {
                continue;
            }

            // 构建 RecoveryShardInfo 对象
            RecoveryShardInfo info = new RecoveryShardInfo();
            info.setIndex(recovery.getString("index"));
            info.setShard(recovery.getString("shard"));
            info.setTime(recovery.getString("time"));
            info.setType(recovery.getString("type"));
            info.setStage(recovery.getString("stage"));

            // 源节点
            info.setSourceHost(recovery.getString("source_host"));
            info.setSourceNode(recovery.getString("source_node"));

            // 目标节点
            info.setTargetHost(recovery.getString("target_host"));
            info.setTargetNode(recovery.getString("target_node"));

            // 快照相关字段（可能为 "n/a"）
            info.setRepository(recovery.getString("repository"));
            info.setSnapshot(recovery.getString("snapshot"));

            // 文件统计
            info.setFiles(parseInt(recovery.getString("files")));
            info.setFilesRecovered(parseInt(recovery.getString("files_recovered")));
            info.setFilesTotal(parseInt(recovery.getString("files_total")));
            info.setFilesPercent(parseBigDecimal(recovery.getString("files_percent")));

            // 字节统计
            info.setBytes(parseLong(recovery.getString("bytes")));
            info.setBytesRecovered(parseLong(recovery.getString("bytes_recovered")));
            info.setBytesTotal(parseLong(recovery.getString("bytes_total")));
            info.setBytesPercent(parseBigDecimal(recovery.getString("bytes_percent")));

            // 事务日志操作数
            info.setTranslogOps(parseInt(recovery.getString("translog_ops")));
            info.setTranslogOpsRecovered(parseInt(recovery.getString("translog_ops_recovered")));
            info.setTranslogOpsPercent(parseBigDecimal(recovery.getString("translog_ops_percent")));

            result.add(info);
        }

        return result;
    }

    // 辅助方法：字符串转 Integer（兼容 null 和 "n/a"）
    private Integer parseInt(String value) {
        if (value == null || "n/a".equals(value)) return null;
        try {
            return Integer.parseInt(value.replaceAll(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 辅助方法：字符串转 Long
    private Long parseLong(String value) {
        if (value == null || "n/a".equals(value)) return null;
        try {
            return Long.parseLong(value.replaceAll(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 辅助方法：百分比字符串转 BigDecimal（如 "0.0%" -> 0.0）
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || "n/a".equals(value)) return null;
        try {
            return new BigDecimal(value.replace("%", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }



    /**
     * 根据分页请求参数从正在迁移的分片集合中获取指定页面的数据。
     *
     * @param allShards 所有正在迁移的分片列表（RecoveryShardInfo 类型）
     * @param pageReq   分页请求参数
     * @return Tuple2<Integer, List<RecoveryShardInfo>> 第一个是总记录数，第二个是当前页的数据列表
     */
    public static Tuple2<Integer, List<RecoveryShardInfo>> getDataPage(List<RecoveryShardInfo> allShards, PageReq pageReq) {
        // 总记录数
        int totalRecords = allShards.size();

        // 计算起始和结束位置
        int start = (pageReq.getPageNum() - 1) * pageReq.getPageSize();
        int end = Math.min(start + pageReq.getPageSize(), totalRecords);

        if (start >= end || start >= totalRecords) {
            return Tuple2.of(0, Collections.emptyList()); // 超出范围则返回空列表
        }

        // 获取当前页的数据
        List<RecoveryShardInfo> currentPageData = allShards.subList(start, end);

        return Tuple2.of(totalRecords, new ArrayList<>(currentPageData));
    }


}