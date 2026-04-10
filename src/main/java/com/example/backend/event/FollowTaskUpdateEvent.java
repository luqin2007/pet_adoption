package com.example.backend.event;

import com.example.backend.entity.FollowTask;
import com.example.backend.entity.User;

/**
 * 创建回访跟踪任务
 */
public record FollowTaskUpdateEvent(FollowTask task, User user) {
}
