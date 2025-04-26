package com.dipper.monitor.service.elastic.index.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.IndexOneOperatorService;
import com.dipper.monitor.service.elastic.index.impl.handlers.IndexCloseHandler;
import com.dipper.monitor.service.elastic.index.impl.handlers.IndexDeleteHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class IndexOneOperatorServiceImpl implements IndexOneOperatorService {

    @Autowired
    private ElasticClientService elasticClientService;


    @Override
    public String openIndexs(String indexs) throws IOException {
        String result = this.elasticClientService.executePostApi(indexs + "/_open", null);
        if (StringUtils.isNotBlank(indexs)) {
            String[] indexes = indexs.split(",");
            for (String item : indexes) {
            }
        }
        return result;
    }

    @Deprecated
    public boolean closeOneIndex(String index) {
        IndexCloseHandler indexCloseHandler = new IndexCloseHandler(elasticClientService);
        return indexCloseHandler.closeOneIndex(index);
    }



    @Deprecated
    public boolean deleteIndex(String index) {
        IndexDeleteHandler indexDeleteHandler = new IndexDeleteHandler(elasticClientService);
        return indexDeleteHandler.deleteIndex(index);

    }

    public boolean forceRefreshIndex(String indexs) throws IOException {
        if (indexs == null) {
            log.error("参数非法：索引名称不能为空");
            return false;
        }
        String api = indexs + "/_refresh";
        String result = this.elasticClientService.executePostApi(api, null);
        JSONObject json = JSON.parseObject(result);
        if (json.containsKey("_shards")) {
            Integer failed = json.getJSONObject("_shards").getInteger("failed");
            if (failed.intValue() == 0) {
                log.info("索引刷新成功: {}", indexs);
                return true;
            } else {
                log.error("索引刷新失败: {}", json.toJSONString());
                return false;
            }
        } else {
            log.error("索引刷新失败: 返回结果中不包含_shards字段");
            return false;
        }
    }

    public boolean segmentForceMerge(String index) throws IOException {
        if (StringUtils.isBlank(index)) {
            log.error("索引信息为空");
            return false;
        }
        String result = this.elasticClientService.executePostApi(index + "/_forcemerge", null);
        JSONObject json = JSON.parseObject(result);
        if (json.containsKey("_shards") && json.getJSONObject("_shards").getInteger("failed") == 0) {
            return true;
        } else {
            log.error("段合并失败: {}", result);
            return false;
        }
    }
}
