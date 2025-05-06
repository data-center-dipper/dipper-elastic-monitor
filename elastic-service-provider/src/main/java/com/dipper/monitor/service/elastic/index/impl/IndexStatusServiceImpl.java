package com.dipper.monitor.service.elastic.index.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.IndexStatusService;
import com.dipper.monitor.utils.elastic.EsDateUtils;
import com.dipper.monitor.utils.elastic.IndexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
public class IndexStatusServiceImpl implements IndexStatusService {

    @Autowired
    private ElasticClientService elasticClientService;

    @Deprecated
    public boolean isIndexCanWrite(String index) throws IOException {
        String indexSetting = this.elasticClientService.executeGetApi(index + "/_settings");
        JSONObject indexSettingObj = JSON.parseObject(indexSetting);
        String indexReadOnly = (String) JSONPath.eval(indexSettingObj, "$..settings.index.blocks.read_only[/XMLSchemaType]");
        if ("true".equals(indexReadOnly)) {
            return false;
        }
        String canWrite = (String) JSONPath.eval(indexSettingObj, "$..settings.index.blocks.write[0]");
        if ("false".equals(canWrite))
            return true;
        return !"true".equals(canWrite);
    }

    @Deprecated
    public List<String> getIndexCanClose(String indexs) throws IOException {
        List<String> canCloseIndex = new ArrayList<>();
        for (String indexName : indexs.split(",")) {
            if (IndexUtils.isIndexNameContainSpecialChar(indexName)) {
                log.error("索引：{} 包含特殊字符串不予关闭操作", indexName);
                continue;
            }
            if (!indexName.contains("-")) {
                log.error("索引：{} 不包含横杠不予关闭操作", indexName);
                continue;
            }
            String[] parts = indexName.split("-");
            String datePart = parts[parts.length - 2];

            if (!isAllNumber(datePart)) {
                log.error("索引：{} 倒数第二个信息不是数字不予关闭操作", indexName);
                continue;
            }

            Integer nowDateTime = null;
            switch (datePart.length()) {
                case 4:
                    nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt("yyyy"));
                    break;
                case 6:
                    nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt("yyyyMM"));
                    break;
                case 8:
                    nowDateTime = Integer.valueOf(EsDateUtils.getNowDateInt("yyyyMMdd"));
                    break;
                default:
                    log.error("索引：{} 日期格式不支持", indexName);
                    continue;
            }

            int indexDateTimeInt = Integer.parseInt(datePart);
            if (nowDateTime <= indexDateTimeInt || !isIndexOpen(indexName)) {
                log.error("索引：{} 是今天或者已经关闭状态不予关闭操作", indexName);
                continue;
            }

            canCloseIndex.add(indexName);
        }
        return canCloseIndex;
    }

    private static final Pattern pattern = Pattern.compile("[0-9]*");

    public  boolean isAllNumber(String dest) {
        return pattern.matcher(dest).matches();
    }


    @Override
    public boolean isIndexFreeze(String index) {
        return false;
    }

    public boolean isIndexOpen(String indexName) throws IOException {
        String result = this.elasticClientService.executeGetApi("/_cat/indices/" + indexName + "?format=json");
        JSONArray array = JSON.parseArray(result);
        JSONObject obj = array.getJSONObject(0);
        return "open".equals(obj.getString("status"));
    }

    public boolean isIndexClose(String index) throws IOException {
        String result = this.elasticClientService.executeGetApi("/_cat/indices/" + index + "?format=json");
        JSONArray array = JSON.parseArray(result);
        JSONObject obj = array.getJSONObject(0);
        return "close".equals(obj.getString("status"));
    }


}
