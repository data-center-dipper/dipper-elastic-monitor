package com.dipper.monitor.service.elastic.index;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.index.IndexEntity;
import com.dipper.monitor.entity.elastic.index.IndexListView;
import com.dipper.monitor.entity.elastic.index.IndexPageReq;
import com.dipper.monitor.entity.elastic.index.IndexSetting;
import com.dipper.monitor.utils.Tuple2;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public interface ElasticRealIndexService {
    /**
     * 查看索引Map
     * @param getSetting
     * @return
     * @throws IOException
     */
    Map<String, IndexEntity>  listIndexMap(boolean getSetting) throws IOException;

    /**
     * 查看索引列表
     */
    List<IndexEntity> listIndexList(boolean setting, boolean alias, String status) throws IOException;

    /**
     * 根据前缀获取索引信息
     * @param indexPrefix 索引前缀 xx-log
     * @param indexXing 索引前缀 xx-log*
     * @return
     * @throws IOException
     */
    List<IndexEntity> listIndexNameByPrefix(String indexPrefix, String indexXing) throws IOException;

    /**
     * 根据索引模式获取索引列表
     * @param indexPatterns 索引模式，如 lcc-log-YYYYMMDD
     * @return 索引列表
     * @throws IOException 异常
     */
    List<String> listIndexNameByPrefix(String indexPatterns) throws IOException;

    /**
     * 根据索引模式获取索引列表
     * @param indexPatterns
     * @param indexPrefix
     * @param indexXing
     * @return
     * @throws IOException
     */
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

    /**
     * 查看某个索引的模版信息
     * @param indexName
     * @return
     */
    JSONObject indexTemplate(String indexName);

    /**
     * 根据别名获取索引列表
     */
    List<IndexEntity> listIndexByAliasName(String aliasName, boolean b, boolean b1, String indexState) throws IOException;

    /**
     * 根据索引前缀获取索引列表
     * @param indexPatterns xx-yyyyMMdd-* 这种格式
     */
    List<IndexEntity> listIndexNameByIndexPatterns(String indexPatterns, boolean b, boolean b1, String indexState) throws IOException;

    public List<IndexEntity> listIndexByPrefix(boolean setting, String indexPrefix, String indexXing) throws IOException;

    public Map<String, IndexSetting> getGlobalIndexSettingFromEs() throws IOException;
}
