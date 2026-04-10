package com.example.backend.event;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.RescueTaskRecord;

/**
 * 更新救助任务状态
 * - 通知救助任务申请人相关结果
 */
public record RescueTaskStatusEvent(RescueTask task, RescueTaskRecord record) {
}
