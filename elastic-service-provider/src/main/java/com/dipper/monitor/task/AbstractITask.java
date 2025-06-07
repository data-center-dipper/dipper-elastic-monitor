package com.dipper.monitor.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public abstract class AbstractITask implements ITask {

    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 模板方法，供 Quartz Job 调用，做统一前置控制
     */
    public final void run() {
        if (!isEnabled()) {
            log.warn("任务 {} 已被禁用，跳过本次执行", getTaskName());
            return;
        }
        try {
            execute();
        } catch (Exception e) {
            log.error("任务 {} 执行失败", getTaskName(), e);
        }
    }
}