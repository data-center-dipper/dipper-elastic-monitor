package com.dipper.monitor.service.elastic.data;


import com.dipper.monitor.entity.elastic.data.ImportDataReq;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;

public interface DataImportService {
    String importData(ImportDataReq importDataReq);
    ProgressInfo getImportProgress(String taskId);
}