package com.dipper.monitor.service.elastic.slowsearch.impl;

import com.dipper.monitor.entity.elastic.slowsearch.kill.KillTimeoutRecord;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillPageReq;
import com.dipper.monitor.entity.elastic.slowsearch.kill.KillQueryResult;
import com.dipper.monitor.mapper.KillTimeoutRecordMapper;
import com.dipper.monitor.service.elastic.slowsearch.SlowQueryKillStoreService;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class SlowQueryKillStoreServiceImpl implements SlowQueryKillStoreService {

    @Autowired
    private KillTimeoutRecordMapper killTimeoutRecordMapper;
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void saveKillRecord(KillQueryResult result) {
        if (result == null || result.getQueryId() == null) {
            log.warn("保存终止记录失败: 结果为空或查询ID为空");
            return;
        }
        
        try {
            KillTimeoutRecord record = new KillTimeoutRecord();
            record.setQueryId(result.getQueryId());
            record.setTaskId(result.getTaskId());
            record.setKillTime(DATE_FORMAT.format(result.getKillTime()));
            record.setStatus("killed");
            record.setReason(result.isSuccess() ? "手动终止" : result.getReason());
            
            // 插入记录
            killTimeoutRecordMapper.insert(record);
            log.info("成功保存查询终止记录: queryId={}, success={}", result.getQueryId(), result.isSuccess());
        } catch (Exception e) {
            log.error("保存终止记录异常: {}", e.getMessage(), e);
        }
    }

    @Override
    public List<KillTimeoutRecord> queryByTimeRange(Date startTime, Date endTime) {
        try {
            // 格式化日期
            String formattedStartTime = startTime != null ? DATE_FORMAT.format(startTime) : null;
            String formattedEndTime = endTime != null ? DATE_FORMAT.format(endTime) : null;
            
            // 调用Mapper查询
            List<KillTimeoutRecord> records = killTimeoutRecordMapper.queryByTimeRange(formattedStartTime, formattedEndTime);
            
            return records != null ? records : new ArrayList<>();
        } catch (Exception e) {
            log.error("按时间范围查询终止记录异常: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 分页查询终止记录
     * @param killPageReq 分页请求
     * @return 终止记录列表和总数
     */
    public Tuple2<List<KillTimeoutRecord>, Long> queryKillRecordPage(KillPageReq killPageReq) {
        try {
            // 转换为通用查询请求

            
            // 计算分页参数
            int offset = (killPageReq.getPageNum() - 1) * killPageReq.getPageSize();
            int limit = killPageReq.getPageSize();
            
            // 查询总数
            long total = killTimeoutRecordMapper.count(killPageReq);
            
            // 如果没有记录，直接返回空列表
            if (total == 0) {
                return new Tuple2<>(new ArrayList<>(), 0L);
            }
            
            // 查询分页数据
            List<KillTimeoutRecord> records = killTimeoutRecordMapper.queryPage(killPageReq, offset, limit);
            
            return new Tuple2<>(records, total);
        } catch (Exception e) {
            log.error("查询终止记录异常: {}", e.getMessage(), e);
            return new Tuple2<>(new ArrayList<>(), 0L);
        }
    }
    
    /**
     * 获取终止记录详情
     * @param recordId 记录ID
     * @return 终止记录详情
     */
    public KillTimeoutRecord getKillRecordDetail(Integer recordId) {
        try {
            return killTimeoutRecordMapper.getById(recordId);
        } catch (Exception e) {
            log.error("获取终止记录详情异常: {}", e.getMessage(), e);
            return null;
        }
    }
}
