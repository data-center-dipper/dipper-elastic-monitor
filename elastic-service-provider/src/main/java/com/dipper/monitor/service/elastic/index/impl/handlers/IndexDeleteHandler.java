package com.dipper.monitor.service.elastic.index.impl.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.utils.elastic.EsDateUtils;
import com.dipper.monitor.utils.elastic.IndexUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
public class IndexDeleteHandler extends AbstractIndexHandler {

    public IndexDeleteHandler(ElasticClientService elasticClientService) {
        super(elasticClientService);
    }

    /**
     * 尝试删除指定索引，根据不同的条件决定是否允许操作。
     *
     * @param index 索引名称
     * @return 是否成功删除索引
     * @throws IllegalArgumentException 如果输入参数无效或不符合预期
     */
    public boolean deleteIndex(String index) throws IllegalArgumentException {
        // 验证索引名称是否为空
        if (StringUtils.isBlank(index)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }
        // 检查是否尝试删除所有索引
        if ("*".equals(index.trim())) {
            throw new IllegalArgumentException("不能删除所有索引");
        }
        // 不支持一次删除多个索引
        if (index.contains(",")) {
            throw new IllegalArgumentException("不支持一次删除多个索引");
        }
        // 检查索引名称是否包含特殊字符
        if (IndexUtils.isIndexNameContainSpecialChar(index)) {
            throw new IllegalArgumentException("索引包含不允许的特殊字符，无法删除");
        }
        // 索引名必须包含'-'
        if (!index.contains("-")) {
            throw new IllegalArgumentException("索引名必须包含'-'，无法执行删除操作");
        }

        // 解析日期部分
        String[] parts = index.split("-");
        String datePart = parts[parts.length - 2];
        Integer nowDateTime;
        String indexPrefix;

        try {
            switch (datePart.length()) {
                case 4:
                    nowDateTime = EsDateUtils.getNowDateInt("yyyy");
                    indexPrefix = extractPrefix(index, datePart);
                    break;
                case 6:
                    nowDateTime = EsDateUtils.getNowDateInt("yyyyMM");
                    indexPrefix = extractPrefix(index, datePart);
                    break;
                case 8:
                    nowDateTime = EsDateUtils.getNowDateInt("yyyyMMdd");
                    indexPrefix = extractPrefix(index, datePart);
                    break;
                default:
                    throw new IllegalArgumentException("未知的日期格式: " + datePart);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("处理索引 " + index + " 的日期部分时出错: " + e.getMessage(), e);
        }

        int indexDateTimeInt = Integer.parseInt(datePart);

        try {
            // 如果索引日期与当前日期相同，则检查是否是最新的索引
            if (nowDateTime.intValue() == indexDateTimeInt) {
                List<IndexEntity> list = elasticRealIndexService.listIndexNameByPrefix(indexPrefix, indexPrefix + "*");
                if (!list.isEmpty() && index.equals(list.get(0))) {
                    throw new IllegalArgumentException("此索引为最新索引，不能删除");
                }
            }
            // 执行删除索引操作
            return doDeleteIndex(index);
        } catch (IOException e) {
            throw new RuntimeException("删除索引 " + index + " 时发生异常: " + e.getMessage(), e);
        }
    }

    private String extractPrefix(String index, String datePart) {
        return index.substring(0, index.indexOf(datePart) - 1);
    }

    public boolean doDeleteIndex(String name) throws IOException {
        try {
            log.info("deleteIndex 索引：{} 操作开始", name);
            String response = this.elasticClientService.executeDeleteApi(name.trim(), null);
            JSONObject result = JSON.parseObject(response);
            if (result.containsKey("acknowledged") && result.getBooleanValue("acknowledged")) {
                log.info("deleteIndex 索引：{} 操作成功", name);
                return true;
            } else {
                log.error("删除索引 {} 时ES返回信息为：{}", name, response);
                return false;
            }
        } catch (Exception e) {
            log.error("删除索引 {} 时发生异常: {}", name, e.getMessage(), e);
            throw new RuntimeException("删除索引失败，请检查相关配置或联系管理员", e);
        }
    }
}