package com.dipper.monitor.entity.elastic.thread.pengding;

import lombok.Data;


@Data
public class PendingTaskView {
    private Long insertOrder;
    private String priority;
    private String source;
    private Long timeInQueueMillis;
    private Boolean executing;

    public static PendingTaskView from(PendingTask task) {
        PendingTaskView view = new PendingTaskView();
        view.setInsertOrder(task.getInsertOrder());
        view.setPriority(task.getPriority());
        view.setSource(task.getSource());
        view.setTimeInQueueMillis(task.getTimeInQueueMillis());
        view.setExecuting(task.getExecuting());
        return view;
    }
}
