package com.dipper.monitor.service.elastic.data;

import com.dipper.monitor.entity.elastic.data.ExportDataReq;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;

import java.util.Map;

public interface DataExportService {

    /**
     * 导出数据
     * @return 导出任务ID
     */
    String exportData(ExportDataReq exportDataReq);
    
    /**
     * 获取导出进度
     * @param taskId 导出任务ID
     * @return 进度信息，包含进度百分比和状态
     */
    ProgressInfo getExportProgress(String taskId);

    Map<String, ProgressInfo> getExportTasks();
}
