package com.dipper.monitor.service.elastic.alians.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.alians.IndexAlians;
import com.dipper.monitor.enums.elastic.ElasticRestApi;
import com.dipper.monitor.service.elastic.alians.ElasticAliansService;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.nio.entity.NStringEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class ElasticAliansServiceImpl implements ElasticAliansService {

    @Autowired
    private ElasticClientService elasticClientService ;

    @Override
    public boolean isWriteEx(String aliasData) {
    if (1 < countAliansWrite(aliasData)) {
         return true;
      }
   return false;
      }

    @Override
    public String getAliansMaxIndexRolling(String aliansData) {
        JSONObject jsonObject = JSON.parseObject(aliansData);

        // Convert the keys (index names) from the JSON object into a list.
        List<String> indexNames = new ArrayList<>(jsonObject.keySet());

        // Sort the list of index names lexicographically.
        Collections.sort(indexNames);

        // If the list is not empty, return the last element which represents the maximum value in lexicographical order.
        if (!indexNames.isEmpty()) {
            return indexNames.get(indexNames.size() - 1);
        }

        // Return an empty string if there are no indices.
        return "";
    }

    public int countAliansWrite(String aliasData) {
        Map<String, Set<String>> aliasIndexMap = new HashMap<>();

        try {
            JSONObject json = JSON.parseObject(aliasData);

            for (Map.Entry<String, Object> indexEntry : json.entrySet()) {
                String indexName = indexEntry.getKey();
                JSONObject aliasesJson = ((JSONObject) indexEntry.getValue()).getJSONObject("aliases");

                if (aliasesJson == null) continue; // Skip if no aliases found

                for (Map.Entry<String, Object> aliasEntry : aliasesJson.entrySet()) {
                    String aliasName = aliasEntry.getKey();
                    JSONObject aliasDetails = (JSONObject) aliasEntry.getValue();
                    Boolean isWriteIndex = aliasDetails.getBoolean("is_write_index");

                    // Add the index to the set associated with this alias
                    aliasIndexMap.computeIfAbsent(aliasName, k -> new HashSet<>()).add(indexName);

                    // If 'is_write_index' is false or null, we do not remove it from the set.
                    // The logic only counts those sets that have more than one index.
                }
            }
        } catch (Exception e) {
            log.info("解析别名异常：aliasData：{} {}", aliasData, e.getMessage(), e);
            return 0;
        }

        // Check for any alias having more than one write index
        for (Set<String> indexes : aliasIndexMap.values()) {
            if (indexes.size() > 1) {
                return indexes.size(); // Return the size of the first such set found
            }
        }

        return 0; // No alias has more than one write index
    }

    /**
     * 根据索引模式获取别名列表
     * indexPatterns 格式如下 aaa-xxx-20251010-*
     * @param indexPatterns
     * @return
     */
    /**
     * 根据索引模式获取别名列表
     * @param indexPatterns 索引模式
     * @return 别名列表
     */
    @Override
    public List<String> listAliansByIndexPatterns(String indexPatterns) {
        if (StringUtils.isBlank(indexPatterns)) {
            return Collections.emptyList();
        }
        
        try {
            // 使用索引模式查询别名信息
            String aliasData = elasticClientService.executeGetApi(indexPatterns + "/_alias");
            if (StringUtils.isBlank(aliasData)) {
                return Collections.emptyList();
            }
            
            // 解析别名数据
            JSONObject jsonObject = JSON.parseObject(aliasData);
            Set<String> aliasSet = new HashSet<>();
            
            // 遍历所有索引
            for (Map.Entry<String, Object> indexEntry : jsonObject.entrySet()) {
                JSONObject aliasesJson = ((JSONObject) indexEntry.getValue()).getJSONObject("aliases");
                if (aliasesJson == null || aliasesJson.isEmpty()) {
                    continue;
                }
                
                // 将该索引的所有别名添加到集合中
                aliasSet.addAll(aliasesJson.keySet());
            }
            
            return new ArrayList<>(aliasSet);
        } catch (Exception e) {
            log.error("获取索引模式别名列表异常：indexPatterns:{} ex:{}", indexPatterns, e.getMessage(), e);
            return Collections.emptyList();
        }
    }


    public String changeIndexWrite(String indexName, String alias, boolean flag) throws Exception {
        String body = "{\n  \"actions\": [\n    {\n      \"add\": {\n        \"index\": \"" + indexName + "\",\n        \"alias\": \"" + alias + "\",\n        \"is_write_index\":" + flag + "\n      }\n    }\n  ]\n}";

        try {
            NStringEntity entity = new NStringEntity(body);
            try {
                String result = elasticClientService.executePostApi("/_aliases", entity);
                entity.close();
                return result;
            } catch (Throwable throwable) {
                entity.close();
                throw throwable;
            }
        } catch (Exception e) {
            log.error("更新索引不可写异常：index :{} body:{} ex:{}", indexName, body, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<String> listExceptionAlians() throws IOException {
        Map<String, List<IndexAlians>> mebeyEx = getAlainsHiveManyIndex();
        List<String> exAlians = new ArrayList<>();
        for (Map.Entry<String, List<IndexAlians>> item : mebeyEx.entrySet()) {
            String alias = item.getKey();
            String aliasData = this.elasticClientService.executeGetApi(alias + "/_alias");
            boolean isWriteIndexGreaterThanOne = isWriteEx(aliasData);

            if (!isWriteIndexGreaterThanOne) {
                continue;
            }

            exAlians.add(alias);
        }
        return exAlians;
    }

    protected Map<String, List<IndexAlians>> getAlainsHiveManyIndex() throws IOException {
        Map<String, List<IndexAlians>> group = getAliansIndexMap();

        Map<String, List<IndexAlians>> mebeyEx = new HashMap<>();

        for (Map.Entry<String, List<IndexAlians>> item : group.entrySet()) {
            List<IndexAlians> value = item.getValue();
            if (1 < value.size()) {
                mebeyEx.put(item.getKey(), item.getValue());
            }
        }
        return mebeyEx;
    }

    public Map<String, List<IndexAlians>> getAliansIndexMap() throws IOException {
        String result = this.elasticClientService.executeGetApi(ElasticRestApi.ALIASES_LIST.getApiPath());
        if (StringUtils.isBlank(result)) {
            return null;
        }
        String[] lines = result.split("\n");
        Map<String, List<IndexAlians>> map = new HashMap<>();
        for (String line : lines) {
            String[] fields = line.split("\\s+");
            IndexAlians indexAlians = new IndexAlians(fields[0], fields[1], fields[2], fields[3], fields[4]);
            List<IndexAlians> list = map.get(fields[0]);
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(indexAlians);
            map.put(fields[0], list);
        }
        return map;
    }

    @Override
    public Map<String, JSONObject> getAllAliansJson() throws IOException {
        String aliansRep = this.elasticClientService.executeGetApi("/*/_alias");
        if (StringUtils.isBlank(aliansRep)) {
            return Collections.emptyMap();
        }
        JSONObject json = JSON.parseObject(aliansRep);
        Map<String, JSONObject> map = new HashMap<>(json.size());
        for (Map.Entry<String, Object> item : (Iterable<Map.Entry<String, Object>>)json.entrySet()) {
            String index = item.getKey();
            JSONObject value = (JSONObject)item.getValue();
            map.put(index, value);
        }
        return map;
    }
    
    @Override
    public String setAliasReadOnly(String alias) throws Exception {
        // 获取别名对应的索引
        String aliasData = this.elasticClientService.executeGetApi(alias + "/_alias");
        if (StringUtils.isBlank(aliasData)) {
            log.warn("别名 {} 不存在或没有关联索引", alias);
            return "别名不存在或没有关联索引";
        }
        
        JSONObject jsonObject = JSON.parseObject(aliasData);
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String indexName = entry.getKey();
            // 设置索引对应的别名为不可写
            changeIndexWrite(indexName, alias, false);
            log.info("设置别名 {} 在索引 {} 上为只读", alias, indexName);
        }
        
        return "成功设置别名为只读";
    }
    
    @Override
    public String addAlias(String indexName, String aliasName) throws Exception {
        String body = "{\n" +
                "  \"actions\": [\n" +
                "    {\n" +
                "      \"add\": {\n" +
                "        \"index\": \"\" + indexName + \"\",\n" +
                "        \"alias\": \"\" + aliasName + \"\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        try {
            NStringEntity entity = new NStringEntity(body);
            try {
                String result = elasticClientService.executePostApi("/_aliases", entity);
                entity.close();
                log.info("为索引 {} 添加别名 {} 成功", indexName, aliasName);
                return result;
            } catch (Throwable throwable) {
                entity.close();
                log.error("为索引 {} 添加别名 {} 失败: {}", indexName, aliasName, throwable.getMessage(), throwable);
                throw throwable;
            }
        } catch (Exception e) {
            log.error("为索引添加别名异常：index:{} alias:{} ex:{}", indexName, aliasName, e.getMessage(), e);
            throw e;
        }
    }
}
