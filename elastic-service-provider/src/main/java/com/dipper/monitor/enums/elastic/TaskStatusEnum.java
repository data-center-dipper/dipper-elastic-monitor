package com.dipper.monitor.enums.elastic;

import lombok.Getter;

/**
 * 任务状态枚举
 */
@Getter
public enum TaskStatusEnum {

    STOPPED(0, "STOPPED", "已停止"),
    RUNNING(1, "RUNNING", "运行中"),
    PAUSED(2, "PAUSED", "已暂停"),
    ERROR(3, "ERROR", "异常"),
    DELETED(4, "DELETED", "已删除");

    private final Integer code;
    private final String status;  // 英文状态码
    private final String desc;    // 中文描述

    TaskStatusEnum(Integer code, String status, String desc) {
        this.code = code;
        this.status = status;
        this.desc = desc;
    }

    /**
     * 根据 code 获取枚举实例
     */
    public static TaskStatusEnum fromCode(Integer code) {
        for (TaskStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("未知的任务状态 code: " + code);
    }

    /**
     * 根据 status（英文标识）获取枚举实例
     */
    public static TaskStatusEnum fromStatus(String status) {
        for (TaskStatusEnum statusEnum : values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("未知的任务状态 status: " + status);
    }

    /**
     * 根据 desc（中文描述）获取枚举实例
     */
    public static TaskStatusEnum fromDesc(String desc) {
        for (TaskStatusEnum statusEnum : values()) {
            if (statusEnum.getDesc().equals(desc)) {
                return statusEnum;
            }
        }
        throw new IllegalArgumentException("未知的任务状态 desc: " + desc);
    }
}