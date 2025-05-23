package com.dipper.monitor.service.elastic.thread;

import com.dipper.monitor.entity.elastic.thread.ThreadCheckResult;
import com.dipper.monitor.entity.elastic.thread.ThreadHotView;
import com.dipper.monitor.entity.elastic.thread.ThreadPageReq;
import com.dipper.monitor.utils.Tuple2;

import java.util.List;

public interface ThreadManagerService {
    /**
     * 分页查询热点线程
     * @param threadPageReq 分页请求参数
     * @return 线程列表和总数
     */
    List<ThreadHotView> threadPage();

    /**
     * 获取线程详情
     * @param threadId 线程ID
     * @return 线程详情
     */
    ThreadHotView getThreadDetail(Integer threadId);

    /**
     * 刷新线程列表
     * @return 最新线程列表
     */
    List<ThreadHotView> refreshThreadList();
    
    /**
     * 执行线程环境检测
     * @return 线程检测结果
     */
    ThreadCheckResult checkThreadEnvironment();
}
