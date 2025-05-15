package com.dipper.monitor.service.elastic.alians;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.alians.IndexAlians;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ElasticAliansService {
    boolean isWriteEx(String aliansData);

    String getAliansMaxIndexRolling(String aliansData);

    String changeIndexWrite(String indexMax, String alians, boolean b)  throws Exception;

    List<String> listExceptionAlians() throws IOException;

    Map<String, List<IndexAlians>> getAliansIndexMap() throws IOException  ;

    Map<String, JSONObject> getAllAliansJson() throws IOException;

    int countAliansWrite(String aliasResult);

    List<String> listAliansByIndexPatterns(String indexPatterns);
    
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
}
