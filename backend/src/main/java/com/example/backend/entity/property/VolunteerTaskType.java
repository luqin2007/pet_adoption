package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 志愿者任务类型
 */
public enum VolunteerTaskType {
    RESCUE, // 救助任务
    DAILY_DUTY, // 日常值班
    FOLLOW_VISIT, // 回访任务
    MEDICAL_SUPPORT, // 医疗协助
    TRANSPORT, // 物资运输
    EVENT, // 活动支持
    OTHER; // 其他任务

    /**
     * 根据字符串获取枚举
     */
    public static VolunteerTaskType get(String name) {
        try {
            return VolunteerTaskType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("exception.invalidate.volunteer_task_type");
        }
    }
}
