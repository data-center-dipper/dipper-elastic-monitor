package com.dipper.monitor.service.elastic.index.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.index.IndexBatchOperatorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class IndexBatchOperatorServiceImpl implements IndexBatchOperatorService {

    @Autowired
    private ElasticClientService elasticClientService;

    public boolean forceFlushIndexs(String indexs) throws IOException {
        if (indexs == null) {
            log.error("参数非法：索引名称不能为空");
            return false;
        }
        String api = indexs + "/_flush";
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

    public boolean forceClearIndexs(String indexs) throws IOException {
        if (indexs == null) {
            log.error("参数非法：索引名称不能为空");
            return false;
        }
        String api = indexs + "/_cache/clear";
        String result = this.elasticClientService.executePostApi(api, null);
        JSONObject json = JSON.parseObject(result);
        if (json.containsKey("_shards")) {
            Integer failed = json.getJSONObject("_shards").getInteger("failed");
            if (failed.intValue() == 0) {
                log.info("索引缓存清理成功: {}", indexs);
                return true;
            } else {
                log.error("索引缓存清理失败: {}", json.toJSONString());
                return false;
            }
        } else {
            log.error("索引缓存清理失败: 返回结果中不包含_shards字段");
            return false;
        }
    }


    @Deprecated
    public boolean closeIndexs(String indexs) throws IOException {
        if (StringUtils.isBlank(indexs)) {
            log.error("索引信息为空");
            return false;
        }
        String result = this.elasticClientService.executePostApi(indexs + "/_close", null);
        JSONObject json = JSON.parseObject(result);
        if (json.containsKey("acknowledged") && json.getBooleanValue("acknowledged")) {
            if (StringUtils.isNotBlank(indexs)) {
                String[] indexes = indexs.split(",");
                for (String item : indexes) {
                }
            }
            return true;
        } else {
            log.error("关闭索引失败: {}", result);
            return false;
        }
    }

    public boolean openIndexs(String indexs) throws IOException {
        if (StringUtils.isBlank(indexs)) {
            log.error("索引信息为空");
            return false;
        }
        String result = this.elasticClientService.executePostApi(indexs + "/_open", null);
        JSONObject json = JSON.parseObject(result);
        if (json.containsKey("acknowledged") && json.getBooleanValue("acknowledged")) {
            if (StringUtils.isNotBlank(indexs)) {
                String[] indexes = indexs.split(",");
                for (String item : indexes) {
                }
            }
            return true;
        } else {
            log.error("打开索引失败: {}", result);
            return false;
        }
    }

    @Deprecated
    public boolean delIndexs(String indexs) throws IOException {
        if (StringUtils.isBlank(indexs)) {
            log.error("索引信息为空");
            return false;
        }
        String result = this.elasticClientService.executeDeleteApi(indexs, null);
        JSONObject json = JSON.parseObject(result);
        if (json.containsKey("acknowledged") && json.getBooleanValue("acknowledged")) {
            if (StringUtils.isNotBlank(indexs)) {
                String[] indexes = indexs.split(",");
                for (String item : indexes) {
                }
            }
            return true;
        } else {
            log.error("删除索引失败: {}", result);
            return false;
        }
    }


    @Deprecated
    public boolean frozenIndexs(String indexs) throws IOException {
        if (StringUtils.isBlank(indexs)) {
            log.error("索引信息为空");
            return false;
        }
        String result = this.elasticClientService.executePostApi(indexs + "/_freeze", null);
        JSONObject json = JSON.parseObject(result);
        if (json.containsKey("acknowledged") && json.getBooleanValue("acknowledged")) {
            if (StringUtils.isNotBlank(indexs)) {
                String[] indexes = indexs.split(",");
                for (String item : indexes) {
                }
            }
            return true;
        } else {
            log.error("冻结索引失败: {}", result);
            return false;
        }
    }


    public boolean unFrozenIndexs(String indexs) throws IOException {
        if (StringUtils.isBlank(indexs)) {
            log.error("索引信息为空");
            return false;
        }
        String result = this.elasticClientService.executePostApi(indexs + "/_unfreeze", null);
        JSONObject json = JSON.parseObject(result);
        if (json.containsKey("acknowledged") && json.getBooleanValue("acknowledged")) {
            if (StringUtils.isNotBlank(indexs)) {
                String[] indexes = indexs.split(",");
                for (String item : indexes) {
                }
            }
            return true;
        } else {
            log.error("解冻索引失败: {}", result);
            return false;
        }
    }
}
