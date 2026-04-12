package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 救助任务状态
 */
public enum RescueTaskStatus {
    CREATED, // 已创建
    APPROVED, // 审核通过
    PROCESSING, // 正在处理中
    COMPLETED, // 任务完成
    DISCARDED; // 已废弃

    public static RescueTaskStatus get(String name) {
        try {
            return RescueTaskStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.rescue_task_status");
        }
    }
}