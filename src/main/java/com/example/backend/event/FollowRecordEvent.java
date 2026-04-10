package com.example.backend.event;

import com.example.backend.entity.FollowRecord;

/**
 * 添加跟踪任务记录事件
 */
public record FollowRecordEvent(FollowRecord record) {
}
