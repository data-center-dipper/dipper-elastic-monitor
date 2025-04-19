package com.dipper.monitor.service.elastic.alians;

public interface ElasticAliansService {
    boolean isWriteEx(String aliansData);

    String getAliansMaxIndexRolling(String aliansData);

    String changeIndexWrite(String indexMax, String alians, boolean b)  throws Exception;
}
