package com.dipper.monitor.mapper;

import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryPageReq;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface SlowQueryMapper {

    int insertSlowQuery(SlowQueryEntity entity);

    int saveSlowQueries(List<SlowQueryEntity> slowQueries);

    int deleteById(Integer id);

    int updateSlowQuery(SlowQueryEntity entity);

    SlowQueryEntity selectById(Integer id);

    List<SlowQueryEntity> selectByPage(
            @Param("clusterCode") String clusterCode,
            @Param("queryType") String queryType,
            @Param("status") String status,
            @Param("startTime") String startTime,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    int countByPage(
            @Param("clusterCode") String clusterCode,
            @Param("queryType") String queryType,
            @Param("status") String status,
            @Param("startTime") String startTime
    );

    List<SlowQueryEntity> selectByIds(@Param("ids") List<Integer> ids);


    void cleanHistoryData(@Param("retentionDays") int retentionDays);

    int queryPageNum(SlowQueryPageReq pageReq);

    List<SlowQueryEntity> queryPage(SlowQueryPageReq pageReq);
}