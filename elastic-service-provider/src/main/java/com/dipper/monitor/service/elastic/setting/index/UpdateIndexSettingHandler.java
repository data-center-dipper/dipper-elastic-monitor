package com.dipper.monitor.service.elastic.setting.index;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.client.proxy.params.elasticsearch.Response;
import com.dipper.monitor.entity.elastic.index.IndexSettingReq;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.service.elastic.nodes.ElasticRealNodeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/**
 * 修改索引设置的方法
 */
@Slf4j
public class UpdateIndexSettingHandler {

    private final IndexSettingReq indexSettingReq;
    private final ElasticRealIndexService elasticRealIndexService;
    private final ElasticClientService elasticClientService;
    private final ElasticRealNodeService elasticRealNodeService;

    public UpdateIndexSettingHandler(IndexSettingReq indexSettingReq,
                                     ElasticRealIndexService elasticRealIndexService,
                                     ElasticClientService elasticClientService,
                                     ElasticRealNodeService elasticRealNodeService) {
        this.indexSettingReq = indexSettingReq;
        this.elasticRealIndexService = elasticRealIndexService;
        this.elasticClientService = elasticClientService;
        this.elasticRealNodeService = elasticRealNodeService;
    }

    public void handle() {
        try {
            // 1. 参数校验
            validateRequest();

            // 2. 获取原始设置并解码 Base64
            String encodedSetting = indexSettingReq.getSettingBase64();
            String decodedSetting = new String(Base64.getDecoder().decode(encodedSetting));

            // 3. JSON 校验
            validateJson(decodedSetting);

            // 4. 获取当前索引的原始设置信息（可选：做对比）
            JSONObject originalSetting = elasticRealIndexService.getIndexSetting(indexSettingReq.getIndexName());
            if (originalSetting == null) {
                log.warn("未找到索引 {} 的现有配置", indexSettingReq.getIndexName());
            }

            // 5. 执行更新操作
            updateIndexSettings(indexSettingReq.getIndexName(), decodedSetting);

            log.info("索引 {} 设置更新成功", indexSettingReq.getIndexName());

        } catch (IllegalArgumentException e) {
            log.error("参数错误: {}", e.getMessage());
            throw e;
        } catch (IOException e) {
            log.error("IO 异常: 索引设置更新失败", e);
            throw new RuntimeException("IO 异常: 索引设置更新失败", e);
        } catch (Exception e) {
            log.error("未知错误: 索引设置更新失败", e);
            throw new RuntimeException("未知错误: 索引设置更新失败", e);
        }
    }

    /**
     * 请求参数校验
     */
    private void validateRequest() {
        if (indexSettingReq == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }

        if (StringUtils.isBlank(indexSettingReq.getIndexName())) {
            throw new IllegalArgumentException("索引名称不能为空");
        }

        if (StringUtils.isBlank(indexSettingReq.getSettingBase64())) {
            throw new IllegalArgumentException("索引设置内容不能为空");
        }
    }

    /**
     * JSON 内容校验
     */
    private void validateJson(String jsonStr) {
        try {
            JSON.parseObject(jsonStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 格式不正确: " + e.getMessage());
        }
    }

    /**
     * 调用 ES 接口更新索引设置
     */
    private void updateIndexSettings(String indexName, String settingJson) throws Exception {
        String apiPath = "/" + indexName + "/_settings";

        Map<String, String> params = Map.of();

        StringEntity entity = new StringEntity(settingJson, ContentType.APPLICATION_JSON);

        Response response = elasticClientService.executePutApiReturnResponseEx(apiPath, entity);

        if (response != null && response.getStatusLine().getStatusCode() >= 300) {
            throw new IOException("更新索引设置失败: HTTP " + response.getStatusLine().getStatusCode());
        }
    }
}