package com.dipper.monitor.service.elastic.index.impl.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.utils.elastic.EsDateUtils;
import com.dipper.monitor.utils.elastic.IndexUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;

@Slf4j
public class IndexCloseHandler extends AbstractIndexHandler {

    public IndexCloseHandler(ElasticClientService elasticClientService) {
        super(elasticClientService);
    }

    /**
     * 尝试关闭指定索引，根据不同的条件决定是否允许操作。
     *
     * @param index 索引名称
     * @return 是否成功关闭索引
     * @throws IllegalArgumentException 如果输入参数无效或不符合预期
     */
    public boolean closeOneIndex(String index) throws IllegalArgumentException {
        // 检查输入的有效性
        if (StringUtils.isBlank(index)) {
            throw new IllegalArgumentException("索引名称不能为空");
        }
        if ("*".equals(index.trim())) {
            throw new IllegalArgumentException("不能关闭所有索引");
        }
        if (index.contains(",")) {
            throw new IllegalArgumentException("不支持一次关闭多个索引");
        }
        if (IndexUtils.isIndexNameContainSpecialChar(index)) {
            throw new IllegalArgumentException("索引包含不允许的特殊字符，无法关闭");
        }
        if (!index.contains("-")) {
            throw new IllegalArgumentException("索引名必须包含'-'，无法执行关闭操作");
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
        if (nowDateTime < indexDateTimeInt) {
            throw new IllegalArgumentException("索引日期在未来，无法关闭");
        }

        // 检查是否为最新的索引
        try {
            List<String> list = elasticRealIndexService.listIndexNameByPrefix(indexPrefix, indexPrefix + "*");
            if (!list.isEmpty() && index.equals(list.get(0))) {
                throw new IllegalArgumentException("此索引为最新索引，不能关闭");
            }
        } catch (Exception e) {
            throw new RuntimeException("获取索引列表时发生异常: " + e.getMessage(), e);
        }

        // 执行关闭操作
        try {
            JSONObject result = doCloseOneIndex(index);
            return result != null && result.containsKey("acknowledged") && result.getBooleanValue("acknowledged");
        } catch (IOException e) {
            throw new RuntimeException("关闭索引 " + index + " 时发生异常: " + e.getMessage(), e);
        }
    }

    private String extractPrefix(String index, String datePart) {
        return index.substring(0, index.indexOf(datePart) - 1);
    }

    private JSONObject doCloseOneIndex(String name) throws IOException {
        String response = elasticClientService.executePostApi("/" + name + "/_close", null);
        JSONObject result = JSON.parseObject(response);
        if (result == null || !result.containsKey("acknowledged") || !result.getBooleanValue("acknowledged")) {
            log.error("关闭索引时ES返回信息为：{}", response);
            throw new RuntimeException("关闭索引失败，响应内容：" + response);
        }
        return result;
    }
}