package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 志愿者激励状态
 */
public enum VolunteerRewardStatus {
    PENDING, // 待发放
    ISSUED, // 已发放
    CANCELLED; // 已取消

    /**
     * 根据字符串获取枚举
     */
    public static VolunteerRewardStatus get(String name) {
        try {
            return VolunteerRewardStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("exception.invalidate.volunteer_reward_status");
        }
    }
}
