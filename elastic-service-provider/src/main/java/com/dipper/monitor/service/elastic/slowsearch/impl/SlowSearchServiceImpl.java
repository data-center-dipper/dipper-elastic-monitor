package com.dipper.monitor.service.elastic.slowsearch.impl;

import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.slow.QueryOptimizationReq;
import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQueryPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQuerySummaryReq;
import com.dipper.monitor.entity.elastic.slowsearch.slow.SlowQuerySummaryView;
import com.dipper.monitor.entity.elastic.slowsearch.task.SlowQueryTaskEntity;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryView;
import com.dipper.monitor.mapper.KillTimeoutRecordMapper;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.slowsearch.RealSlowSearchService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryKillStoreService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;
import com.dipper.monitor.service.elastic.slowsearch.SlowSearchService;
import com.dipper.monitor.service.elastic.slowsearch.handlers.slow.OneQueryOptimizationHandler;
import com.dipper.monitor.service.elastic.slowsearch.handlers.slow.SlowQuerySummaryHandler;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SlowSearchServiceImpl implements SlowSearchService {

    @Autowired
    private ElasticClientService elasticClientService;
    
    @Autowired
    private KillTimeoutRecordMapper killTimeoutRecordMapper;
    @Autowired
    private SlowQueryStoreService slowQueryStoreService;
    @Autowired
    private RealSlowSearchService realSlowSearchService;
    @Autowired
    private SlowQueryKillStoreService slowQueryKillStoreService;


    @Override
    public Tuple2<List<SlowQueryView>, Integer> slowSearchPage(SlowQueryPageReq pageReq) throws IOException {
        searchNowAndSave();

        int total = slowQueryStoreService.queryPageNum(pageReq);
        List<SlowQueryEntity> slowQueries = slowQueryStoreService.queryPage(pageReq);
        List<SlowQueryView> views = transToSlowQueryView(slowQueries);
        return new Tuple2<>(views, total);
    }

    @Override
    public SlowQuerySummaryView slowSearchSummary(SlowQuerySummaryReq slowQuerySummaryReq) {
        SlowQuerySummaryHandler slowQuerySummaryHandler = new SlowQuerySummaryHandler(realSlowSearchService,
                slowQueryStoreService,slowQueryKillStoreService);
        return slowQuerySummaryHandler.slowSearchSummary(slowQuerySummaryReq);
    }

    @Override
    public String queryOptimization(QueryOptimizationReq queryOptimizationReq) {
        OneQueryOptimizationHandler oneQueryOptimizationHandler = new OneQueryOptimizationHandler(elasticClientService);
        return oneQueryOptimizationHandler.queryOptimization(queryOptimizationReq);
    }

    private void searchNowAndSave() throws IOException {
        // 获取原始数据
        List<SlowQueryTaskEntity> relaSlowQuery = realSlowSearchService.getRelaSlowQuery();

        // 转换为数据库实体类
        List<SlowQueryEntity> slowQueryEntities = realSlowSearchService.transToSlowQueryEntity(relaSlowQuery);

        slowQueryStoreService.saveSlowQueries(slowQueryEntities);
    }

    /**
     * 将 SlowQueryEntity 列表转换为 SlowQueryView 列表
     *
     * @param slowQueries 数据库实体列表
     * @return 视图对象列表
     */
    /**
     * 将 SlowQueryEntity 列表转换为 SlowQueryView 列表
     *
     * @param slowQueries 数据库实体列表
     * @return 视图对象列表
     */
    private List<SlowQueryView> transToSlowQueryView(List<SlowQueryEntity> slowQueries) {
        if (slowQueries == null || slowQueries.isEmpty()) {
            return new ArrayList<>();
        }

        List<SlowQueryView> viewList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 定义目标时间格式

        for (SlowQueryEntity entity : slowQueries) {
            SlowQueryView view = new SlowQueryView();
            try {
                // 使用 BeanUtils 拷贝大部分属性
                BeanUtils.copyProperties(entity, view);
            } catch (Exception e) {
               log.error("copy properties error", e);
            }

            // 手动设置时间字段并格式化
            if (entity.getStartTime() != null) {
                view.setStartTime(sdf.format(entity.getStartTime()));
            } else {
                view.setStartTime(null);
            }

            if (entity.getCollectTime() != null) {
                view.setCollectTime(sdf.format(entity.getCollectTime()));
            } else {
                view.setCollectTime(null);
            }

            viewList.add(view);
        }

        return viewList;
    }




}
