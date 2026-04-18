package com.example.backend.event;

import com.example.backend.entity.FollowTask;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

/**
 * 更新回访任务
 * - 通知志愿者：回访时间变更（来自领养人）
 * - 通知工作人员：回访时间变更（来自领养人）
 *
 * @see com.example.backend.service.AdoptBreadingService#updateFollowTask(Long, com.example.backend.dto.FollowTaskUpdateRequest)
 */
public record FollowTaskUpdateEvent(FollowTask data, User user) implements INotifyEvent<FollowTask> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.FOLLOW_TASK;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return user.is(data.getVolunteerId())
                ? langHelper.get("notification.follow_task_feedback.title")
                : langHelper.get("notification.follow_task_update.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return user.is(data.getVolunteerId())
                ? langHelper.get("notification.follow_task_feedback.content")
                : langHelper.get("notification.follow_task_update.content");
    }
}
