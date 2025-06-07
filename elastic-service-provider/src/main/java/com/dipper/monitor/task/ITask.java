package com.dipper.monitor.task;

public interface ITask {
    String getCron();           // 获取当前cron表达式
    void setCron(String cron); // 支持动态设置cron表达式
    String getAuthor();
    String getJobDesc();
    boolean isEditable();
    void execute();             // 实际执行的方法
    String getTaskName();       // 每个任务唯一标识符，如："clearIndexNoDataTask"
}
