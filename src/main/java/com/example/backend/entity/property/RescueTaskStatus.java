package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 救助任务状态
 */
public enum RescueTaskStatus {
    CREATED(), // 已创建
    APPROVED(CREATED), // 审核通过
    PROCESSING(APPROVED), // 正在处理中
    COMPLETED(APPROVED, PROCESSING), // 任务完成
    DISCARDED(CREATED, APPROVED, PROCESSING); // 已废弃

    private final Set<RescueTaskStatus> previousStatus;

    RescueTaskStatus(RescueTaskStatus first, RescueTaskStatus... other) {
        Set<RescueTaskStatus> statuses = new HashSet<>();
        statuses.add(first);
        statuses.addAll(Arrays.asList(other));
        this.previousStatus = Set.copyOf(statuses);
    }

    RescueTaskStatus() {
        this.previousStatus = Set.of();
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
