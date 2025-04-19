package com.dipper.monitor.task;

public interface ITask {
    public String cron();
    public String author();
    public String jobDesc();
    public Boolean editAble();
}
