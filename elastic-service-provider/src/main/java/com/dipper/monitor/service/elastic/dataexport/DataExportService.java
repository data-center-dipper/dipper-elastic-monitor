package com.dipper.monitor.service.elastic.dataexport;

import java.util.List;
import java.util.Map;

public interface DataExportService {
    /**
     * 搜索索引名称
     * @param keyword 搜索关键词
     * @return 匹配的索引列表
     */
    List<String> searchIndices(String keyword);
    
    /**
     * 导出数据
     * @param index 索引名称
     * @param query 查询条件（JSON格式）
     * @param format 导出格式（json/csv）
     * @param size 导出条数
     * @return 导出任务ID
     */
    String exportData(String index, String query, String format, int size);
    
    /**
     * 获取导出进度
     * @param taskId 导出任务ID
     * @return 进度信息，包含进度百分比和状态
     */
    Map<String, Object> getExportProgress(String taskId);
}
