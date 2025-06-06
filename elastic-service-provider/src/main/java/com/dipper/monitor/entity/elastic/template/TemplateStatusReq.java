package com.dipper.monitor.entity.elastic.template;

import lombok.Data;

@Data
public class TemplateStatusReq {
    private Integer templateId;
    private Boolean enable;
}
