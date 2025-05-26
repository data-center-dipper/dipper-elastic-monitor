package com.dipper.monitor.service.elastic.data.input;

import com.dipper.monitor.entity.elastic.data.ImportDataReq;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class AbstractImportHandler implements ImportHandler {

    protected ImportDataReq importDataReq;
    protected String format;
    protected String filePath;
    protected boolean ignoreErrors;
    protected ProgressUpdater progressUpdater;
    protected int totalLines;
    protected final AtomicInteger errorCount = new AtomicInteger(0);

    /**
     * 构造函数
     * @param importDataReq 导入请求参数
     * @param totalLines 总行数
     * @param progressUpdater 进度更新器
     */
    public AbstractImportHandler(ImportDataReq importDataReq, int totalLines, ProgressUpdater progressUpdater) {
        this.importDataReq = importDataReq;
        this.format = importDataReq.getFormat();
        this.filePath = importDataReq.getFilePath();
        this.ignoreErrors = importDataReq.isIgnoreErrors();
        this.totalLines = totalLines;
        this.progressUpdater = progressUpdater;
    }

    @Override
    public int getErrorCount() {
        return errorCount.get();
    }

    /**
     * 计算当前进度百分比
     */
    protected int calculateProgress(int current, int total) {
        if (total <= 0) return 0;
        return Math.min(95, (int) (((double) current / total) * 95));
    }

    /**
     * 记录错误并根据配置决定是否继续
     * @param message 错误消息
     * @param e 异常
     * @return 是否继续处理（true=继续，false=中断）
     */
    protected boolean handleError(String message, Exception e) {
        log.error(message, e);
        errorCount.incrementAndGet();
        return ignoreErrors; // 如果设置了忽略错误，则返回true继续处理
    }
}
