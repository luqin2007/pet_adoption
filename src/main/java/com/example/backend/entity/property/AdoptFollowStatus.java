package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 领养跟踪任务状态
 */
public enum AdoptFollowStatus {
    PENDING, // 待执行
    NOTIFIED, // 已通知领养人
    IN_PROGRESS, // 执行中
    DELAY, // 推迟
    FINISH; // 已完成

    public static AdoptFollowStatus get(String name) {
        try {
            return AdoptFollowStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("无效跟踪任务状态 " + name);
        }
    }
}

