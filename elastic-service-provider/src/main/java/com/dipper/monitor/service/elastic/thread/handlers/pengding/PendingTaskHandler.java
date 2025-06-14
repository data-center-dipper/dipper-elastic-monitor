package com.dipper.monitor.service.elastic.thread.handlers.pending;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dipper.common.lib.utils.ApplicationUtils;
import com.dipper.monitor.entity.elastic.PageReq;
import com.dipper.monitor.entity.elastic.thread.pengding.PendingTask;
import com.dipper.monitor.entity.elastic.thread.pengding.PendingTaskView;
import com.dipper.monitor.service.elastic.client.ElasticClientService;
import com.dipper.monitor.service.elastic.thread.ThreadManagerService;
import com.dipper.monitor.utils.ListUtils;
import com.dipper.monitor.utils.Tuple2;
import com.dipper.monitor.utils.mock.MockAllData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PendingTaskHandler {

    private final ThreadManagerService threadManagerService;
    private final ElasticClientService elasticClientService;

    public PendingTaskHandler(ThreadManagerService threadManagerService,
                              ElasticClientService elasticClientService) {
        this.threadManagerService = threadManagerService;
        this.elasticClientService = elasticClientService;
    }

    /**
     * 获取并分页返回 pending tasks
     */
    public Tuple2<Integer, List<PendingTaskView>> pendingTasks(PageReq pageReq) throws IOException {
        if(ApplicationUtils.isWindows()){
            return MockAllData.pendingTasks();
        }
        // 1. 调用接口获取原始数据
        String rawResponse = elasticClientService.executeGetApi("/_cluster/pending_tasks");

        // 2. 解析 JSON 响应
        JSONObject jsonObject = JSON.parseObject(rawResponse);
        JSONArray taskArray = jsonObject.getJSONArray("tasks");

        // 3. 将 JSON 数据转换为 Java 对象列表
        List<PendingTask> taskViews = new ArrayList<>();
        if (taskArray != null && !taskArray.isEmpty()) {
            for (int i = 0; i < taskArray.size(); i++) {
                JSONObject taskJson = taskArray.getJSONObject(i);

                PendingTask view = new PendingTask();
                view.setInsertOrder(taskJson.getLong("insert_order"));
                view.setPriority(taskJson.getString("priority"));
                view.setSource(taskJson.getString("source"));
                view.setTimeInQueueMillis(taskJson.getLong("time_in_queue_millis"));
                view.setExecuting(taskJson.getBoolean("executing"));

                taskViews.add(view);
            }
        }

        // 4. 分页处理（使用 ListUtils 工具类）
        int total = taskViews.size();

        int pageNum = pageReq.getPageNum();
        int pageSize = pageReq.getPageSize();

        // 使用 ListUtils.splitListBySize 进行分页
        List<List<PendingTask>> pagedLists = ListUtils.splitListBySize(taskViews, pageSize);

        // 确保请求的页数不超过实际页数
        if (pageNum > pagedLists.size()) {
            return new Tuple2<>(total, new ArrayList<>()); // 返回空列表
        }

        // 获取当前页的数据
        List<PendingTask> currentPage = pagedLists.get(pageNum - 1); // pageNum 是从 1 开始的

        List<PendingTaskView> currentView = trnasferToPendingTaskView(currentPage);

        // 5. 返回结果：总数量 + 当前页数据
        return new Tuple2<>(total, currentView);
    }

    private List<PendingTaskView> trnasferToPendingTaskView(List<PendingTask> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }

        List<PendingTaskView> views = new ArrayList<>(tasks.size());
        for (PendingTask task : tasks) {
            views.add(PendingTaskView.from(task));
        }
        return views;
    }
}