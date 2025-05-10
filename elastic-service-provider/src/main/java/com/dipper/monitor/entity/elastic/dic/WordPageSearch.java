package com.dipper.monitor.entity.elastic.dic;

import lombok.Data;

@Data
public class WordPageSearch {
    private Integer pageSize;
    private Integer pageNum;
    private String keyword;
    private Integer dicId;
    private Integer offset;
}
