package com.dipper.monitor.entity.elastic.dic;

import lombok.Data;

@Data
public class DicPageInfo {
    private Integer pageSize;
    private Integer pageNum;
    private String keyword;
}
