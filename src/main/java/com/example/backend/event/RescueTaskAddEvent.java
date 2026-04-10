package com.example.backend.event;

import com.example.backend.entity.Location;
import com.example.backend.entity.RescueTask;

/**
 * 创建救助任务
 * - 通知管理员审核
 */
public record RescueTaskAddEvent(RescueTask task, Location location) {
}
