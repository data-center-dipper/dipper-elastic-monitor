package com.dipper.monitor.service.elastic.data.input;

public interface ImportHandler extends Runnable {
    /**
     * 处理单行数据
     * @param line 数据行
     * @return 处理是否成功
     */
    boolean processLine(String line);
    
    /**
     * 获取错误计数
     * @return 错误数量
     */
    int getErrorCount();
}
