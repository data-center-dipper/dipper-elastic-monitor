package com.dipper.monitor.service.elastic.data.impl;

import com.dipper.monitor.entity.db.elastic.SunTaskEntity;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskReq;
import com.dipper.monitor.entity.elastic.data.migration.MigrationTaskView;
import com.dipper.monitor.mapper.MigrationParentTaskMapper;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.data.MigrationParentTaskService;
import com.dipper.monitor.service.elastic.data.MigrationSunTaskService;
import com.dipper.monitor.service.elastic.data.migration.ParentTaskSplitHandler;
import com.dipper.monitor.service.elastic.index.ElasticRealIndexService;
import com.dipper.monitor.utils.Tuple2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;

/**
 * 父任务服务实现类
 */
@Service
@Slf4j
public class MigrationParentTaskServiceImpl implements MigrationParentTaskService {

    @Autowired
    private MigrationParentTaskMapper migrationParentTaskMapper;
    @Autowired
    private ElasticClientService elasticClientService;
    @Autowired
    private ElasticRealIndexService elasticRealIndexService;
    @Autowired
    private MigrationSunTaskService migrationSunTaskService;

    /**
     * 分页查询父任务列表
     *
     * @param pageReq 分页参数
     * @return Tuple2<List<MigrationTaskView>, Long> 任务列表 + 总数
     */
    @Override
    public Tuple2<List<MigrationTaskView>, Long> getPoliciesByPage(PageReq pageReq) {
        Assert.notNull(pageReq, "分页参数不能为空");
        int pageNum = pageReq.getPageNum();
        int pageSize = pageReq.getPageSize();

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询当前页数据
        List<MigrationTaskView> tasks = migrationParentTaskMapper.selectTasksByPage(offset, pageSize);

        // 查询总记录数
        long total = migrationParentTaskMapper.countAllTasks();

        return new Tuple2<>(tasks, total);
    }

    /**
     * 添加新任务
     *
     * @param taskReq 请求参数
     */
    @Override
    public void addTask(MigrationTaskReq taskReq) throws IOException {
        Assert.notNull(taskReq, "任务参数不能为空");
        Assert.hasText(taskReq.getSourceClusterId(), "源集群ID不能为空");
        Assert.hasText(taskReq.getTargetClusterId(), "目标集群ID不能为空");
        Assert.hasText(taskReq.getIndexPattern(), "索引匹配模式不能为空");
        Assert.hasText(taskReq.getQueryCondition(), "查询条件不能为空");
        Assert.hasText(taskReq.getGranularity(), "迁移粒度不能为空");
        Assert.hasText(taskReq.getExecutePolicy(), "执行策略不能为空");
        Assert.notNull(taskReq.getConcurrencyLimit(), "并发限制不能为空");

        log.info("新增任务参数: {}", taskReq);

        ParentTaskSplitHandler parentTaskSplitHandler = new ParentTaskSplitHandler(elasticClientService,elasticRealIndexService);
        parentTaskSplitHandler.checkParams(taskReq);
        parentTaskSplitHandler.checkRunParams(taskReq);
        List<SunTaskEntity> subTask = parentTaskSplitHandler.splitTask(taskReq);
        log.info("拆分后的子任务数量: {}", subTask.size());
        if(subTask.isEmpty()){
            throw new RuntimeException("拆分后的子任务数量为0");
        }

        migrationParentTaskMapper.insertTask(taskReq);


        migrationSunTaskService.insertTask(subTask);
    }

    /**
     * 更新任务信息
     *
     * @param taskReq 请求参数
     */
    @Override
    public void updateTask(MigrationTaskReq taskReq) {
        Assert.notNull(taskReq, "任务参数不能为空");
        Assert.notNull(taskReq.getId(), "任务ID不能为空");

        log.info("更新任务: {}", taskReq);

        migrationParentTaskMapper.updateTask(taskReq);
    }

    /**
     * 根据ID删除任务
     *
     * @param id 任务ID
     */
    @Override
    public void deleteTask(Integer id) {
        Assert.notNull(id, "任务ID不能为空");

        log.info("删除任务: ID={}", id);

        int rowsAffected = migrationParentTaskMapper.deleteTaskById(id.longValue());

        if (rowsAffected <= 0) {
            throw new RuntimeException("未找到对应的任务，删除失败");
        }
    }

    /**
     * 查询单个任务详情
     *
     * @param id 任务ID
     * @return MigrationTaskView
     */
    @Override
    public MigrationTaskView getOneTask(Integer id) {
        Assert.notNull(id, "任务ID不能为空");

        log.info("查询任务详情: ID={}", id);

        MigrationTaskView task = migrationParentTaskMapper.selectTaskById(id.longValue());

        if (task == null) {
            throw new RuntimeException("未找到对应的任务");
        }

        return task;
    }

    @Override
    public MigrationTaskView getOneTaskByTaskId(String taskId) {
        Assert.notNull(taskId, "任务taskId不能为空");

        log.info("查询任务详情: taskId={}", taskId);

        MigrationTaskView task = migrationParentTaskMapper.getOneTaskByTaskId(taskId);

        if (task == null) {
            throw new RuntimeException("未找到对应的任务");
        }

        return task;
    }
}