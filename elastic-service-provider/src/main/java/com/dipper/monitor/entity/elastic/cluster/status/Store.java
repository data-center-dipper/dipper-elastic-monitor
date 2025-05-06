package com.dipper.monitor.entity.elastic.cluster.status;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Store {
    private long size_in_bytes;
    private long total_data_set_size_in_bytes;
    private long reserved_in_bytes;
}
