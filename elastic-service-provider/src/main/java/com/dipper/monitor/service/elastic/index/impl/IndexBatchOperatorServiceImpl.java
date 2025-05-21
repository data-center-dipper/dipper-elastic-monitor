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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class IndexBatchOperatorServiceImpl implements IndexBatchOperatorService {

    @Autowired
    private ElasticClientService elasticClientService;

    // ==================== 公共方法 ====================

    /**
     * 标准化执行索引操作
     */
    private boolean executeIndexOperation(String indices, String operationPath) throws IOException {
        if (StringUtils.isBlank(indices)) {
            log.error("索引名称为空");
            return false;
        }

        String api = indices + operationPath;
        String result = elasticClientService.executePostApi(api, null);

        try {
            JSONObject json = JSON.parseObject(result);
            if (json.containsKey("_shards")) {
                Integer failed = json.getJSONObject("_shards").getInteger("failed");
                boolean success = failed == 0;
                logResult(success, operationPath, indices, result);
                return success;
            } else if (json.containsKey("acknowledged") && json.getBooleanValue("acknowledged")) {
                logResult(true, operationPath, indices, result);
                return true;
            } else {
                logResult(false, operationPath, indices, result);
                return false;
            }
        } catch (Exception e) {
            log.error("解析响应失败，operation={}, response={}", operationPath, result, e);
            return false;
        }
    }

    /**
     * 打印操作结果日志
     */
    private void logResult(boolean success, String operation, String indices, String result) {
        String logMsg = String.format("%s %s: %s", operation, success ? "成功" : "失败", result);
        if (success) {
            log.info(logMsg);
        } else {
            log.error(logMsg);
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 将 List<String> 或 String[] 转换为逗号连接的字符串
     */
    private String formatIndices(List<String> indexList) {
        if (indexList == null || indexList.isEmpty()) {
            return "";
        }
        return indexList.stream().filter(Objects::nonNull).map(String::trim).filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));
    }

    private String formatIndices(String... indices) {
        if (indices == null || indices.length == 0) {
            return "";
        }
        return Arrays.stream(indices).filter(Objects::nonNull).map(String::trim).filter(StringUtils::isNotBlank)
                .collect(Collectors.joining(","));
    }

    // ==================== 实现接口方法 ====================

    @Override
    public boolean forceFlushIndexs(String indexs) throws IOException {
        return executeIndexOperation(indexs, "/_flush");
    }

    @Override
    public boolean forceClearIndexs(String indexs) throws IOException {
        return executeIndexOperation(indexs, "/_cache/clear");
    }

    @Deprecated
    @Override
    public boolean closeIndexs(String indexs) throws IOException {
        return executeIndexOperation(indexs, "/_close");
    }

    @Override
    public boolean openIndexs(String indexs) throws IOException {
        return executeIndexOperation(indexs, "/_open");
    }

    @Deprecated
    @Override
    public boolean delIndexs(String indexs) throws IOException {
        if (StringUtils.isBlank(indexs)) {
            log.error("索引信息为空");
            return false;
        }
        String result = elasticClientService.executeDeleteApi(indexs, null);
        JSONObject json = JSON.parseObject(result);
        if (json.containsKey("acknowledged") && json.getBooleanValue("acknowledged")) {
            log.info("删除索引成功: {}", indexs);
            return true;
        } else {
            log.error("删除索引失败: {}", result);
            return false;
        }
    }

    @Override
    public boolean frozenIndexs(String indexs) throws IOException {
        return executeIndexOperation(indexs, "/_freeze");
    }

    @Override
    public boolean unFrozenIndexs(String indexs) throws IOException {
        return executeIndexOperation(indexs, "/_unfreeze");
    }

    // ==================== 新增常用操作 ====================

    @Override
    public boolean refreshIndexs(String indexs) throws IOException {
        return executeIndexOperation(indexs, "/_refresh");
    }

    @Override
    public boolean optimizeIndexs(String indexs, int maxNumSegments) throws IOException {
        if (StringUtils.isBlank(indexs)) {
            log.error("索引信息为空");
            return false;
        }
        String api = indexs + "/_forcemerge?max_num_segments=" + maxNumSegments;
        String result = elasticClientService.executePostApi(api, null);
        JSONObject json = JSON.parseObject(result);
        if (json.containsKey("_shards")) {
            Integer failed = json.getJSONObject("_shards").getInteger("failed");
            boolean success = failed == 0;
            log.info("合并段 {}，结果: {}", success ? "成功" : "失败", result);
            return success;
        } else {
            log.error("合并段失败: {}", result);
            return false;
        }
    }

    @Override
    public boolean rolloverIndex(String aliasName, String newIndexName) throws IOException {
        String api = aliasName + "/_rollover/" + newIndexName;
        String result = elasticClientService.executePostApi(api, null);
        JSONObject json = JSON.parseObject(result);
        if (json.containsKey("acknowledged") && json.getBooleanValue("acknowledged")) {
            log.info("rollover 操作成功: {}", result);
            return true;
        } else {
            log.error("rollover 操作失败: {}", result);
            return false;
        }
    }

    // ==================== 对 List 和 Array 的兼容支持 ====================

    @Override
    public boolean forceFlushIndexs(List<String> indexList) throws IOException {
        return forceFlushIndexs(formatIndices(indexList));
    }

    @Override
    public boolean forceClearIndexs(List<String> indexList) throws IOException {
        return forceClearIndexs(formatIndices(indexList));
    }

    @Override
    public boolean closeIndexs(List<String> indexList) throws IOException {
        return closeIndexs(formatIndices(indexList));
    }

    @Override
    public boolean openIndexs(List<String> indexList) throws IOException {
        return openIndexs(formatIndices(indexList));
    }

    @Override
    public boolean delIndexs(List<String> indexList) throws IOException {
        return delIndexs(formatIndices(indexList));
    }

    @Override
    public boolean frozenIndexs(List<String> indexList) throws IOException {
        return frozenIndexs(formatIndices(indexList));
    }

    @Override
    public boolean unFrozenIndexs(List<String> indexList) throws IOException {
        return unFrozenIndexs(formatIndices(indexList));
    }

    @Override
    public boolean refreshIndexs(List<String> indexList) throws IOException {
        return refreshIndexs(formatIndices(indexList));
    }

    @Override
    public boolean optimizeIndexs(List<String> indexList, int maxNumSegments) throws IOException {
        return optimizeIndexs(formatIndices(indexList), maxNumSegments);
    }
}