package com.dipper.monitor.entity.elastic.thread.hot;

import lombok.Data;

import java.util.List;

@Data
public class HotThreadMiddle {
    NodeMetadata nodeMetadata ;
    List<HotThreadMeta> hotThreadMetas ;
}
