package com.dipper.monitor.entity.elastic.index;

import lombok.Data;

@Data
public class IndexSettingReq {
    // 索引名称
    private String indexName;
    // 索引设置 json
    private String settingBase64;
}
