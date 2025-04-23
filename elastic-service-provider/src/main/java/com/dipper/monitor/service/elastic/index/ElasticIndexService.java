package com.dipper.monitor.service.elastic.index;

import com.dipper.monitor.entity.elastic.index.IndexEntity;

import java.util.Map;

public interface ElasticIndexService {
    Map<String, IndexEntity>  listIndexMap(boolean b);
}
