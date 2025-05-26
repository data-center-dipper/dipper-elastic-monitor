package com.dipper.monitor.entity.elastic.data;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImportDataReq {
    // 目标索引名称
    private String index;
    
    // 文件格式：json或csv
    private String format;
    
    // 上传的文件
    private MultipartFile dataFile;
    private String filePath;

    // 是否忽略错误继续导入
    private boolean ignoreErrors;
}
