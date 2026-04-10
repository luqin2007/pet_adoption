package com.example.backend.event;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.RescueTaskAssign;

import java.util.List;

/**
 * 救助任务分配
 * - 通知分配用户
 * - 通知任务创建者
 */
public record RescueTaskAssignEvent(RescueTask task, List<RescueTaskAssign> assigns) {
}
