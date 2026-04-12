package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.EnumSet;
import java.util.Locale;

/**
 * 志愿者申请状态
 */
public enum VolunteerApplicationStatus {
    SUBMITTED, // 已提交
    UNDER_REVIEW(SUBMITTED), // 审核中
    APPROVED(SUBMITTED, UNDER_REVIEW), // 已通过
    REJECTED(SUBMITTED, UNDER_REVIEW), // 已拒绝
    CANCELED(SUBMITTED, UNDER_REVIEW); // 已撤回

    private final EnumSet<VolunteerApplicationStatus> previousStatus;

    VolunteerApplicationStatus(VolunteerApplicationStatus first, VolunteerApplicationStatus... other) {
        this.previousStatus = EnumSet.of(first, other);
    }

    VolunteerApplicationStatus() {
        this.previousStatus = EnumSet.noneOf(VolunteerApplicationStatus.class);
    }

    /**
     * 切换到当前状态时需要工作人员权限
     */
    public boolean requireWorker() {
        return this == UNDER_REVIEW || this == APPROVED || this == REJECTED;
    }

    /**
     * 切换到当前状态时需要附加说明
     */
    public boolean isReviewStatus() {
        return this == APPROVED || this == REJECTED;
    }

    public boolean canSwitchFrom(VolunteerApplicationStatus previousStatus) {
        return this.previousStatus.contains(previousStatus);
    }

    /**
     * 根据字符串获取枚举
     */
    public static VolunteerApplicationStatus get(String name) {
        try {
            return VolunteerApplicationStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("无效申请状态 " + name);
        }
    }
}
