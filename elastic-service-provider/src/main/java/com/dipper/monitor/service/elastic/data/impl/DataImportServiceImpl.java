package com.dipper.monitor.service.elastic.data.impl;

import com.dipper.monitor.entity.elastic.data.ImportDataReq;
import com.dipper.monitor.entity.elastic.data.ProgressInfo;
import com.dipper.monitor.service.elastic.data.DataImportService;
import com.dipper.monitor.service.elastic.data.input.ImportTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class DataImportServiceImpl implements DataImportService {

    @Value("${app.upload.temp-dir:./temp}")
    private String tempDir;

    // 线程池用于执行导入任务
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    // 存储任务ID和任务的映射关系
    private final Map<String, ImportTask> taskMap = new ConcurrentHashMap<>();

    @Override
    public String importData(ImportDataReq importDataReq) throws Exception {
        // 生成任务ID
        String taskId = UUID.randomUUID().toString();
        
        // 确保临时目录存在
        Path tempDirPath = Paths.get(tempDir);
        if (!Files.exists(tempDirPath)) {
            Files.createDirectories(tempDirPath);
        }
        
        // 从请求对象中获取文件
        MultipartFile file = importDataReq.getDataFile();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("未提供有效的数据文件");
        }
        
        // 保存上传的文件到临时目录
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String tempFileName = taskId + fileExtension;
        Path tempFilePath = tempDirPath.resolve(tempFileName);
        
        file.transferTo(tempFilePath.toFile());
        log.info("文件已保存到临时目录: {}", tempFilePath);
        
        // 设置文件路径到请求对象
        importDataReq.setFilePath(tempFilePath.toString());
        
        // 创建并启动导入任务
        ImportTask task = new ImportTask(taskId, importDataReq);
        taskMap.put(taskId, task);
        executorService.submit(task);
        
        return taskId;
    }

    @Override
    public ProgressInfo getImportProgress(String taskId) {
        ImportTask task = taskMap.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任务不存在: " + taskId);
        }
        return task.getProgressInfo();
    }
}