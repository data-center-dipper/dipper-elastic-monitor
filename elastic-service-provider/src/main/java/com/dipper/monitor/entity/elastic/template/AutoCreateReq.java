package com.dipper.monitor.entity.elastic.template;

import lombok.Data;

@Data
public class AutoCreateReq {
    private Integer id;
    // 是否开启自动创建
    private Boolean autoCreate;
    // 自动创建未来几个周期
    private Integer rollingPeriod;
}
