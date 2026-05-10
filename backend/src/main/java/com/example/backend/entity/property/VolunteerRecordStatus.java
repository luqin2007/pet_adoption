package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 志愿者服务记录状态
 */
public enum VolunteerRecordStatus {
    DRAFT(null), // 草稿
    SUBMITTED(DRAFT), // 已提交
    APPROVED(SUBMITTED), // 已审核通过
    REJECTED(SUBMITTED); // 已驳回

    private final VolunteerRecordStatus previousStatus;

    VolunteerRecordStatus(VolunteerRecordStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public boolean canChangeFrom(VolunteerRecordStatus status) {
        return this.previousStatus == status;
    }

    public boolean isReview() {
        return this == APPROVED || this == REJECTED;
    }

    /**
     * 根据字符串获取枚举
     */
    public static VolunteerRecordStatus get(String name) {
        try {
            return VolunteerRecordStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("exception.invalidate.volunteer_record_status");
        }
    }
}
