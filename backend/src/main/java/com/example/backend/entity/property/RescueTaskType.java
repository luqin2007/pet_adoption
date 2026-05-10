package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 救助任务类型
 */
public enum RescueTaskType {
    FIND, // 发现流浪宠物
    MEDICAL, // 医疗救助
    OTHER; // 其他

    public static RescueTaskType get(String name) {
        try {
            return RescueTaskType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.rescue_task_type");
        }
    }
}