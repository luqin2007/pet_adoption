package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 救助任务变更类型
 */
public enum RescueTaskAction {
    CREATE, // 创建任务
    UPDATE, // 修改任务
    STATUS, // 修改状态
    FORK; // 产生子任务

    public static RescueTaskAction get(String name) {
        try {
            return RescueTaskAction.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.rescue_task_action");
        }
    }
}
