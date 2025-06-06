package com.dipper.monitor.service.elastic.shard.impl.handler.remove;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.entity.elastic.shard.ShardMigrationReq;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.nio.entity.NStringEntity;

import java.io.IOException;

@Slf4j
public class MigrateShardHandler {

    private ElasticClientService elasticClientService;

    public MigrateShardHandler(ElasticClientService elasticClientService) {
        this.elasticClientService = elasticClientService;
    }

    public boolean migrateShard(ShardMigrationReq migrationReq) throws IOException {
        if (migrationReq.getIndex() == null || migrationReq.getShard() == null ||
                migrationReq.getFromNode() == null || migrationReq.getToNode() == null) {
            throw new IllegalArgumentException("迁移参数不完整");
        }

        if (migrationReq.getFromNode().equals(migrationReq.getToNode())) {
            throw new IllegalArgumentException("源节点和目标节点不能相同");
        }

//        if (ApplicationUtils.isWindows()) {
//            // 在Windows开发环境中模拟成功
//            log.info("模拟分片迁移：{}", JSON.toJSONString(migrationReq));
//            return true;
//        }

        String requestBody = String.format(
                "{\n" +
                        "  \"commands\": [\n" +
                        "    {\n" +
                        "      \"move\": {\n" +
                        "        \"index\": \"%s\",\n" +
                        "        \"shard\": %d,\n" +
                        "        \"from_node\": \"%s\",\n" +
                        "        \"to_node\": \"%s\"\n" +
                        "      }\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}",
                migrationReq.getIndex(),
                migrationReq.getShard(),
                migrationReq.getFromNode(),
                migrationReq.getToNode()
        );
        try {

            // 创建请求实体
            NStringEntity entity = new NStringEntity(requestBody, org.apache.http.entity.ContentType.APPLICATION_JSON);

            // 执行分片迁移请求
            String response = elasticClientService.executePostApi("/_cluster/reroute", entity);
            log.info("分片迁移响应：{}", response);

            // 检查响应是否包含错误信息
            JSONObject responseJson = JSON.parseObject(response);
            if (responseJson.containsKey("acknowledged") && responseJson.getBoolean("acknowledged")) {
                log.info("分片迁移请求已确认：从 {} 节点迁移索引 {} 的分片 {} 到 {} 节点",
                        migrationReq.getFromNode(),
                        migrationReq.getIndex(),
                        migrationReq.getShard(),
                        migrationReq.getToNode());
                return true;
            } else {
                log.error("分片迁移请求未被确认：{}", response);
                return false;
            }
        } catch (Exception e) {
            log.error("分片迁移失败", e);
            throw new IOException("分片迁移失败: " + e.getMessage(), e);
        }
    }
}
