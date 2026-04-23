package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.EnumSet;
import java.util.Locale;

/**
 * 救助任务状态
 */
public enum RescueTaskStatus {
    CREATED(), // 已创建
    APPROVED(CREATED), // 审核通过
    PROCESSING(APPROVED), // 正在处理中
    COMPLETED(APPROVED, PROCESSING), // 任务完成
    DISCARDED(CREATED, APPROVED, PROCESSING); // 已废弃

    private final EnumSet<RescueTaskStatus> previousStatus;

    RescueTaskStatus(RescueTaskStatus first, RescueTaskStatus... other) {
        this.previousStatus = EnumSet.of(first, other);
    }

    RescueTaskStatus() {
        this.previousStatus = EnumSet.noneOf(RescueTaskStatus.class);
    }

    public boolean canChangeFrom(RescueTaskStatus status) {
        return this.previousStatus.contains(status);
    }

    public static RescueTaskStatus get(String name) {
        try {
            return RescueTaskStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.rescue_task_status");
        }
    }
}