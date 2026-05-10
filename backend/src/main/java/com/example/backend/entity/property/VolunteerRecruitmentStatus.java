package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 志愿者招募状态
 */
public enum VolunteerRecruitmentStatus {
    DRAFT, // 草稿
    PUBLISHED, // 招募中
    CLOSED; // 已关闭

    public boolean canChangeFrom(VolunteerRecruitmentStatus from) {
        return switch (this) {
            case DRAFT -> from != CLOSED;
            case PUBLISHED -> from == DRAFT;
            case CLOSED -> true;
        };
    }

    /**
     * 根据字符串获取枚举
     */
    public static VolunteerRecruitmentStatus get(String name) {
        try {
            return VolunteerRecruitmentStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("exception.invalidate.volunteer_recruitment_status");
        }
    }
}
