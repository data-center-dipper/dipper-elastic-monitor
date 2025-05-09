package com.dipper.monitor.entity.elastic.dic;

import lombok.Data;

@Data
public class WordPageInfo {
    private Integer pageSize;
    private Integer pageNum;
    private String keyword;
    private String dicName;
}
