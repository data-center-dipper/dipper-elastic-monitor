package com.dipper.monitor.service.elastic.data.impl;

import com.dipper.monitor.entity.db.elastic.SunTaskEntity;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.data.migration.sun.SunRunTaskReq;
import com.dipper.monitor.entity.elastic.data.migration.sun.SunTaskView;
import com.dipper.monitor.mapper.MigrationSunTaskMapper;
import com.dipper.monitor.service.elastic.data.MigrationSunTaskService;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
@Slf4j
public class MigrationSunTaskServiceImpl implements MigrationSunTaskService {

    @Autowired
    private MigrationSunTaskMapper migrationSunTaskMapper;

    /**
     * 分页查询子任务
     */
    @Override
    public Tuple2<List<SunTaskView>, Long> getSunTaskByPage(PageReq pageReq) {
        Assert.notNull(pageReq, "分页参数不能为空");
        int pageNum = pageReq.getPageNum();
        int pageSize = pageReq.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 查询子任务列表（从数据库获取实体）
        List<SunTaskEntity> entities = migrationSunTaskMapper.selectSubtasksByPage(offset, pageSize);

        // 转换为视图对象（过滤不必要字段）
        List<SunTaskView> views = entities.stream()
                .map(this::convertToView)
                .toList();

        // 获取总数
        long total = migrationSunTaskMapper.countAllSubtasks();

        return new Tuple2<>(views, total);
    }

    private SunTaskView convertToView(SunTaskEntity entity) {
        if (entity == null) {
            return null;
        }
        SunTaskView view = new SunTaskView();
        view.setId(entity.getId());
        view.setParentTaskId(entity.getParentTaskId());
        view.setIndexName(entity.getIndexName());
        view.setStartTime(entity.getStartTime());
        view.setEndTime(entity.getEndTime());
        view.setStatus(entity.getStatus());
        view.setRetryCount(entity.getRetryCount());
        view.setErrorLog(entity.getErrorLog());
        view.setCreatedAt(entity.getCreatedAt());
        view.setUpdatedAt(entity.getUpdatedAt());
        return view;
    }

    @Override
    public void insertTask(List<SunTaskEntity> subTask) {
        if(subTask == null && subTask.isEmpty()) {
            return;
        }
        for (SunTaskEntity task : subTask) {
            migrationSunTaskMapper.insertSubtask(task);
        }
    }

    /**
     * 运行子任务（插入一条记录）
     */
    @Override
    public void runTask(SunRunTaskReq sunRunTaskReq) {
        Assert.notNull(sunRunTaskReq, "子任务参数不能为空");
        Assert.notNull(sunRunTaskReq.getParentTaskId(), "父任务ID不能为空");
        Assert.hasText(sunRunTaskReq.getIndexName(), "索引名称不能为空");
        Assert.hasText(sunRunTaskReq.getStartTime(), "开始时间不能为空");
        Assert.hasText(sunRunTaskReq.getEndTime(), "结束时间不能为空");

        log.info("运行子任务: {}", sunRunTaskReq);

    }
}