package com.dipper.monitor.service.elastic.index;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.entity.elastic.index.IndexListView;
import com.dipper.monitor.entity.elastic.index.IndexPageReq;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public interface ElasticRealIndexService {
    Map<String, IndexEntity>  listIndexMap(boolean getSetting) throws IOException;

    List<IndexEntity> listIndexList(boolean setting, boolean alias, String status) throws IOException;

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

    /**
     * 创建索引
     *
     * @param firstIndexName
     * @return
     */
    String createIndex(String firstIndexName);
    String createIndex(String firstIndexName, JSONObject templateJson) throws UnsupportedEncodingException;

    /**
     * 分页获取索引列表
     */
    Tuple2<List<IndexListView>, Long> indexPageList(IndexPageReq indexPageReq) throws IOException;
}
