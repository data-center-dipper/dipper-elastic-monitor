package com.dipper.monitor.service.elastic.setting;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.index.IndexSettingReq;

public interface IndexSettingService {
    /**
     * 查看索引的设置
     * @param indexName
     * @return
     */
    JSONObject getIndexSetting(String indexName);

    /**
     * 查看索引的mapping
     * @param indexName
     * @return
     */
    JSONObject getMappingByIndexName(String indexName);

    /**
     * 更新索引的设置信息
     * @param indexSettingReqn
     */
    void updateIndexSetting(IndexSettingReq indexSettingReqn);
}
