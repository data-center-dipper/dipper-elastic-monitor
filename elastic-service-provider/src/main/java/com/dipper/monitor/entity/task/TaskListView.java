package com.dipper.monitor.entity.task;

import lombok.Data;

import java.util.Date;

@Data
public class TaskListView {
    private Integer id;
    private String taskName;            // 任务名称
    private String annotationType;       // 注解类型名称
    private String className;            // 类名
    private String methodName;           // 方法名
    private String cron;                 // cron表达式
    private Long fixedRate;              // 固定速率
    private Long fixedDelay;             // 固定延迟
    private String author;               // 作者
    private String groupName;            // 组名
    private String jobDesc;              // 任务描述
    private Boolean editAble;            // 是否可编辑
    private String additionalAttributes; // 其他属性（JSON格式）
    private String status;
    private Date createTime;             // 创建时间
    private Date updateTime;             // 更新时间
}
