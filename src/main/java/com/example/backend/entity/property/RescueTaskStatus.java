package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.EnumMap;
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

    private static final EnumMap<RescueTaskStatus, EnumSet<RescueTaskStatus>> PREVIOUS_STATUSES;

    static {
        PREVIOUS_STATUSES = new EnumMap<>(RescueTaskStatus.class);
        PREVIOUS_STATUSES.put(CREATED, EnumSet.noneOf(RescueTaskStatus.class));
        PREVIOUS_STATUSES.put(APPROVED, EnumSet.of(CREATED));
        PREVIOUS_STATUSES.put(PROCESSING, EnumSet.of(APPROVED));
        PREVIOUS_STATUSES.put(COMPLETED, EnumSet.of(APPROVED, PROCESSING));
        PREVIOUS_STATUSES.put(DISCARDED, EnumSet.of(CREATED, APPROVED, PROCESSING));
    }

    RescueTaskStatus(RescueTaskStatus first, RescueTaskStatus... other) {
    }

    RescueTaskStatus() {
    }

    public boolean canChangeFrom(RescueTaskStatus status) {
        return PREVIOUS_STATUSES.get(this).contains(status);
    }

    public static RescueTaskStatus get(String name) {
        try {
            return RescueTaskStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.rescue_task_status");
        }
    }
}
