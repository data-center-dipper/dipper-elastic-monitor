package com.dipper.monitor.service.elastic.index;

import com.dipper.monitor.entity.elastic.index.IndexEntity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ElasticRealIndexService {
    Map<String, IndexEntity>  listIndexMap(boolean b) throws IOException;

    List<IndexEntity> listIndexList(boolean b, boolean b1, String o) throws IOException;

    List<IndexEntity> listIndexNameByPrefix(String indexPrefix, String indexXing) throws IOException;

    /**
     * 根据索引模式获取索引列表
     * @param indexPatterns 索引模式，如 lcc-log-YYYYMMDD
     * @return 索引列表
     * @throws IOException 异常
     */
    List<String> listIndexNameByPrefix(String indexPatterns) throws IOException;

    List<IndexEntity> listIndexNameByPrefix(String indexPatterns,String indexPrefix, String indexXing) throws IOException;

    Map<String, IndexEntity> listIndexPatternMapThread(boolean b, String indexPatternPrefix, String indexXing) throws IOException;
}
