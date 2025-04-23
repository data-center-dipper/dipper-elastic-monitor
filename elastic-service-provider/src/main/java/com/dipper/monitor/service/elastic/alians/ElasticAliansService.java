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
}
