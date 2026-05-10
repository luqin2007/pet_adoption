package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.EnumMap;
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

    private static final EnumMap<VolunteerApplicationStatus, EnumSet<VolunteerApplicationStatus>> PREVIOUS_STATUSES;

    static {
        PREVIOUS_STATUSES = new EnumMap<>(VolunteerApplicationStatus.class);
        PREVIOUS_STATUSES.put(SUBMITTED, EnumSet.noneOf(VolunteerApplicationStatus.class));
        PREVIOUS_STATUSES.put(UNDER_REVIEW, EnumSet.of(SUBMITTED));
        PREVIOUS_STATUSES.put(APPROVED, EnumSet.of(SUBMITTED, UNDER_REVIEW));
        PREVIOUS_STATUSES.put(REJECTED, EnumSet.of(SUBMITTED, UNDER_REVIEW));
        PREVIOUS_STATUSES.put(CANCELED, EnumSet.of(SUBMITTED, UNDER_REVIEW));
    }

    VolunteerApplicationStatus(VolunteerApplicationStatus first, VolunteerApplicationStatus... other) {
    }

    VolunteerApplicationStatus() {
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
        return PREVIOUS_STATUSES.get(this).contains(previousStatus);
    }

    /**
     * 根据字符串获取枚举
     */
    public static VolunteerApplicationStatus get(String name) {
        try {
            return VolunteerApplicationStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("exception.invalidate.volunteer_application_status");
        }
    }
}
