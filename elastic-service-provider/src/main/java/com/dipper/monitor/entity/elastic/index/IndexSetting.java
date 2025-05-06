package com.dipper.monitor.entity.elastic.index;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class IndexSetting {
    private String index;
    private JSONObject settingData;
    private Boolean freeze;
    private Boolean blocksWrite;
}
