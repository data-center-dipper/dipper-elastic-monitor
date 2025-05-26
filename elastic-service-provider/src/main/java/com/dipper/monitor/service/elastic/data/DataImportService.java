package com.dipper.monitor.service.elastic.data;


import com.dipper.monitor.entity.elastic.data.ImportDataReq;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import org.springframework.web.multipart.MultipartFile;

public interface DataImportService {
    /**
     * 导入数据
     * @param importDataReq 导入请求参数（包含上传的文件）
     * @return 任务ID
     */
    String importData(ImportDataReq importDataReq) throws Exception;
    
    /**
     * 获取导入进度
     * @param taskId 任务ID
     * @return 进度信息
     */
    ProgressInfo getImportProgress(String taskId);
}