package com.dipper.monitor.service.elastic.index;

import com.dipper.monitor.entity.elastic.index.IndexEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ElasticIndexService {
    Map<String, IndexEntity>  listIndexMap(boolean b) throws IOException;

    List<IndexEntity> listIndexList(boolean b, boolean b1, String o) throws IOException;

    List<String> listIndexNameByPrefix(String indexParttonFromWeb, String s) throws IOException;
}
