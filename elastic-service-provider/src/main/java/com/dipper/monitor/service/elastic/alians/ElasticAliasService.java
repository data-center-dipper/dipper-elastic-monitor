package com.dipper.monitor.service.elastic.alians;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.alians.IndexAlias;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ElasticAliasService {
    boolean isWriteEx(String aliasData);

    String getAliasMaxIndexRolling(String aliasData);

    String changeIndexWrite(String indexMax, String alias, boolean b)  throws Exception;

    List<String> listExceptionAlias() throws IOException;

    Map<String, List<IndexAlias>> getAliasIndexMap() throws IOException  ;

    Map<String, JSONObject> getAllAliasJson() throws IOException;

    int countAliasWrite(String aliasResult);

    List<String> listAliasByIndexPatterns(String indexPatterns);
    
    /**
     * 设置别名为只读
     * @param alias 别名
     * @return 操作结果
     * @throws Exception 异常信息
     */
    String setAliasReadOnly(String alias) throws Exception;
    
    /**
     * 为索引添加别名
     * @param indexName 索引名称
     * @param aliasName 别名
     * @return 操作结果
     * @throws Exception 异常信息
     */
    String addAlias(String indexName, String aliasName) throws Exception;

    List<String> aliasNames(String nameLike) throws IOException;
}
