package com.dipper.monitor.service.elastic.slowsearch.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.db.elastic.SlowQueryEntity;
import com.dipper.monitor.entity.elastic.slowsearch.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryTaskEntity;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryView;
import com.dipper.monitor.mapper.KillTimeoutRecordMapper;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.slowsearch.RealSlowSearchService;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryStoreService;
import com.dipper.monitor.service.elastic.slowsearch.SlowSearchService;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

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

    private static final String TASKS_API = "/_tasks";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 缓存慢查询列表，避免频繁请求ES
    private List<SlowQueryView> cachedQueryList = new ArrayList<>();
    
    // 存储终止超时记录
    private List<KillTimeoutRecord> killTimeoutRecords = new CopyOnWriteArrayList<>();
    
    @Override
    public Tuple2<List<SlowQueryView>, Integer> slowSearchPage(SlowQueryPageReq pageReq) throws IOException {
        searchNowAndSave();

        int total = slowQueryStoreService.queryPageNum(pageReq);
        List<SlowQueryEntity> slowQueries = slowQueryStoreService.queryPage(pageReq);
        List<SlowQueryView> views = transToSlowQueryView(slowQueries);
        return new Tuple2<>(views, total);
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
