package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 志愿者档案状态
 */
public enum VolunteerProfileStatus {
    ACTIVE, // 启用
    DISABLED; // 停用

    /**
     * 根据字符串获取枚举
     */
    public static VolunteerProfileStatus get(String name) {
        try {
            return VolunteerProfileStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("无效志愿者档案状态 " + name);
        }
    }
}
