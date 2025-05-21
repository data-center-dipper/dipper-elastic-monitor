package com.dipper.monitor.service.elastic.index.impl.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

@Slf4j
public class IndexListByAliasHandler extends AbstractIndexHandler {

    public IndexListByAliasHandler(ElasticClientService elasticClientService) {
        super(elasticClientService);
    }


    /**
     * 根据别名名称获取关联的索引列表。
     *
     * @param aliasName 别名名称
     * @param setting   是否包含索引设置
     * @param alians    是否包含别名信息
     * @param status    索引状态过滤条件（可为空）
     * @return List<IndexEntity> 索引实体列表
     * @throws IOException 如果请求失败
     */
    public List<IndexEntity> listIndexByAliasName(String aliasName, boolean setting, boolean alians, String status) throws IOException {
        if (StringUtils.isBlank(aliasName)) {
            throw new IllegalArgumentException("aliasName 不能为空");
        }

        // 构建请求路径：/_alias/your_alias_name
        String apiPath = "/_alias/" + aliasName;

        // 发送 GET 请求
        String response = elasticClientService.executeGetApi(apiPath);
        // 别名与索引的关系
        Map<String, String> indexToAliasMap = parseAliasResponse(response);

        if(indexToAliasMap.isEmpty()){
            log.warn("索引别名[{}]不存在",aliasName);
            return Collections.emptyList();
        }


        List<IndexEntity> indexDetailList = new ArrayList<>();
        for (Map.Entry<String, String> item: indexToAliasMap.entrySet()) {
            String indexName = item.getKey();
            String indexAlias = item.getValue();

            String api = "/_cat/indices/"+indexName+"?format=json";
            String indexStr = elasticClientService.executeGetApi(api);
            JSONArray jsonArray = JSON.parseArray(indexStr);

            for (Object object : jsonArray) {
                JSONObject obj = (JSONObject) object;
                String statusInDb = obj.getString("status");
                if (StringUtils.isNotBlank(status) && !status.equals(statusInDb)) {
                    continue;
                }
                try {
                    IndexEntity indexEntity = convertToIndexEntity(obj, setting, alians);
                    indexDetailList.add(indexEntity);
                } catch (IOException e) {
                    log.error("处理索引数据时发生错误", e);
                }
            }
        }

        return indexDetailList;
    }

    public Map<String, String> parseAliasResponse(String jsonResponse) {
        JSONObject root = JSONObject.parseObject(jsonResponse);
        Map<String, String> indexAliasMap = new HashMap<>();

        for (String indexName : root.keySet()) {
            JSONObject indexObj = root.getJSONObject(indexName);
            JSONObject aliases = indexObj.getJSONObject("aliases");

            for (String aliasName : aliases.keySet()) {
                indexAliasMap.put(indexName, aliasName);
            }
        }

        return indexAliasMap;
    }

}
