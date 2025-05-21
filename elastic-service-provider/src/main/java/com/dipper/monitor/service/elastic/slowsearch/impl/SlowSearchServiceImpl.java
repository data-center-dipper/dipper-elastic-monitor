package com.dipper.monitor.service.elastic.slowsearch.impl;

import com.alibaba.fastjson.JSONObject;
import com.dipper.monitor.entity.elastic.slowsearch.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.SlowQueryView;
import com.dipper.monitor.mapper.KillTimeoutRecordMapper;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.slowsearch.SlowSearchService;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    
    private static final String TASKS_API = "/_tasks";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // 缓存慢查询列表，避免频繁请求ES
    private List<SlowQueryView> cachedQueryList = new ArrayList<>();
    
    // 存储终止超时记录
    private List<KillTimeoutRecord> killTimeoutRecords = new CopyOnWriteArrayList<>();
    
    @Override
    public Tuple2<List<SlowQueryView>, Long> queryPage(SlowQueryPageReq pageReq) {
        // 如果缓存为空，则刷新查询列表
        if (cachedQueryList.isEmpty()) {
            refreshQueryList();
        }
        
        List<SlowQueryView> filteredList = cachedQueryList;
        
        // 根据搜索条件过滤
        if (StringUtils.isNotBlank(pageReq.getSearchText())) {
            String keyword = pageReq.getSearchText().toLowerCase();
            filteredList = filteredList.stream()
                    .filter(query -> 
                        (query.getIndexName() != null && query.getIndexName().toLowerCase().contains(keyword)) ||
                        (query.getQueryContent() != null && query.getQueryContent().toLowerCase().contains(keyword)) ||
                        (query.getNodeId() != null && query.getNodeId().toLowerCase().contains(keyword)) ||
                        (query.getTaskId() != null && query.getTaskId().toLowerCase().contains(keyword)))
                    .collect(Collectors.toList());
        }
        
        // 根据查询类型过滤
        if (StringUtils.isNotBlank(pageReq.getQueryType())) {
            filteredList = filteredList.stream()
                    .filter(query -> pageReq.getQueryType().equals(query.getQueryType()))
                    .collect(Collectors.toList());
        }
        
        // 根据状态过滤
        if (StringUtils.isNotBlank(pageReq.getStatus())) {
            filteredList = filteredList.stream()
                    .filter(query -> pageReq.getStatus().equals(query.getStatus()))
                    .collect(Collectors.toList());
        }
        
        // 根据索引名称过滤
        if (StringUtils.isNotBlank(pageReq.getIndexName())) {
            filteredList = filteredList.stream()
                    .filter(query -> query.getIndexName() != null && 
                                    query.getIndexName().contains(pageReq.getIndexName()))
                    .collect(Collectors.toList());
        }
        
        // 根据执行时间过滤
        if (pageReq.getMinExecutionTime() != null) {
            filteredList = filteredList.stream()
                    .filter(query -> query.getExecutionTime() >= pageReq.getMinExecutionTime())
                    .collect(Collectors.toList());
        }
        
        if (pageReq.getMaxExecutionTime() != null) {
            filteredList = filteredList.stream()
                    .filter(query -> query.getExecutionTime() <= pageReq.getMaxExecutionTime())
                    .collect(Collectors.toList());
        }
        
        // 计算总数
        long total = filteredList.size();
        
        // 分页
        int start = (pageReq.getPageNum() - 1) * pageReq.getPageSize();
        int end = Math.min(start + pageReq.getPageSize(), filteredList.size());
        
        if (start >= filteredList.size()) {
            return new Tuple2<>(new ArrayList<>(), total);
        }
        
        List<SlowQueryView> pagedList = filteredList.subList(start, end);
        
        return new Tuple2<>(pagedList, total);
    }
    
    @Override
    public SlowQueryView getQueryDetail(Integer queryId) {
        // 从缓存中查找指定ID的查询
        return cachedQueryList.stream()
                .filter(query -> queryId.equals(query.getId()))
                .findFirst()
                .orElse(null);
    }
    
    @Override
    public boolean killQuery(Integer queryId) {
        try {
            // 获取查询详情
            SlowQueryView query = getQueryDetail(queryId);
            if (query == null || !"running".equals(query.getStatus())) {
                return false;
            }
            
            // 调用ES API终止任务
            String taskId = query.getTaskId();
            if (StringUtils.isBlank(taskId)) {
                return false;
            }
            
            String cancelTaskUrl = TASKS_API + "/" + taskId + "/_cancel";
            String response1 = elasticClientService.executeGetApi(cancelTaskUrl);
            JSONObject response = JSONObject.parseObject(response1);
            // 更新缓存中的状态
            query.setStatus("killed");
            
            boolean success = response != null && response.containsKey("acknowledged") && 
                   response.getBoolean("acknowledged");
                   
            // 如果终止失败，记录超时
            if (!success) {
                recordKillTimeout(queryId, "ES API响应超时");
            }
            
            return success;
        } catch (Exception e) {
            log.error("终止查询失败: {}", e.getMessage(), e);
            // 记录超时异常
            recordKillTimeout(queryId, "终止异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<SlowQueryView> refreshQueryList() {
        return List.of();
    }

    @Override
    public void recordKillTimeout(Integer queryId, String reason) {
        try {
            SlowQueryView query = getQueryDetail(queryId);
            if (query == null) {
                log.warn("记录终止超时失败: 未找到查询ID {}", queryId);
                return;
            }
            
            KillTimeoutRecord record = new KillTimeoutRecord();
            // 不需要设置ID，数据库会自动生成
            record.setQueryId(queryId);
            record.setIndexName(query.getIndexName());
            record.setQueryType(query.getQueryType());
            record.setKillTime(DATE_FORMAT.format(new Date()));
            record.setExecutionTime(query.getExecutionTime());
            record.setStatus("killed"); // 设置为已终止状态
            record.setReason(reason);
            record.setNodeId(query.getNodeId());
            record.setTaskId(query.getTaskId());
            record.setQueryContent(query.getQueryContent());
            // createTime由数据库自动设置
            
            // 将记录保存到数据库
            killTimeoutRecordMapper.insert(record);
            log.info("记录查询终止超时: ID={}, 原因={}", queryId, reason);
        } catch (Exception e) {
            log.error("记录终止超时异常: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public Tuple2<List<KillTimeoutRecord>, Long> queryKillTimeoutPage(SlowQueryPageReq pageReq) {
        // 计算分页参数
        int offset = (pageReq.getPageNum() - 1) * pageReq.getPageSize();
        int limit = pageReq.getPageSize();
        
        // 查询总数
        long total = killTimeoutRecordMapper.count(pageReq);
        
        // 如果没有记录，直接返回空列表
        if (total == 0) {
            return new Tuple2<>(new ArrayList<>(), 0L);
        }
        
        // 查询分页数据
        List<KillTimeoutRecord> records = killTimeoutRecordMapper.queryPage(pageReq, offset, limit);
        
        return new Tuple2<>(records, total);
    }
    
    @Override
    public KillTimeoutRecord getKillTimeoutDetail(Integer recordId) {
        return killTimeoutRecordMapper.getById(recordId);
    }
}
