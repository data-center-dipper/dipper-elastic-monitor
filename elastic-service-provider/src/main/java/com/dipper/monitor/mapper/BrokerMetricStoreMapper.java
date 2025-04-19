package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.db.elastic.BrokerMetricEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BrokerMetricStoreMapper {

    Long findLastBatchId(@Param("moduleName") String moduleName);

    void storeBrokerMetric(BrokerMetricEntity brokerMetric);

    /**
     * 批量插入BrokerMetricEntity记录
     */
    void batchInsert(@Param("entities") List<BrokerMetricEntity> entities);

    List<BrokerMetricEntity> getMetricByMetricKey(@Param("clusterCode") String clusterCode,
                                                  @Param("modelName") String modelName,
                                                  @Param("brokerName") String brokerName,
                                                  @Param("sectionName") String sectionName,
                                                  @Param("metricKey") String metricKey,
                                                  @Param("startTime") String startTime,
                                                  @Param("endTime")String endTime);
}