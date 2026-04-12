package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 志愿者激励类型
 */
public enum VolunteerRewardType {
    MATERIAL, // 物资奖励
    CERTIFICATE, // 荣誉证书
    POINTS, // 积分
    SERVICE_HOURS, // 公益时长证明
    CASH, // 现金/补贴
    OTHER; // 其他

    /**
     * 根据字符串获取枚举
     */
    public static VolunteerRewardType get(String name) {
        try {
            return VolunteerRewardType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("无效激励类型 " + name);
        }
    }
}
