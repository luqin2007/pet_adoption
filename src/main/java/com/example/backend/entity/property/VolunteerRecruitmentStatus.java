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

    /**
     * 根据字符串获取枚举
     */
    public static VolunteerRecruitmentStatus get(String name) {
        try {
            return VolunteerRecruitmentStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("无效招募状态 " + name);
        }
    }
}
