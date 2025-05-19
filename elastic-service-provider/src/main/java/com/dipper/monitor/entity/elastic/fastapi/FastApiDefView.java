package com.dipper.monitor.entity.elastic.fastapi;

import lombok.Data;

@Data
public class FastApiDefView {
    private String id;
    private String apiName; // 中文名称
    private String apiDesc; // 描述信息
    private String method;// get post head  put delete
    private String apiPath; // 路径 /_cat/health 比如
    private String body; // 请求体
    private String headers; // 请求头
}
