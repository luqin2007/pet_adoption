package com.example.backend.event;

import com.example.backend.entity.FollowTask;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

/**
 * 创建回访任务
 * - 通知志愿者：提醒执行新的回访任务
 * - 通知领养者：即将进行回访
 *
 * @see com.example.backend.service.AdoptBreadingService#addFollowTask(Long, com.example.backend.dto.FollowTaskAddRequest)
 */
public record FollowTaskAddEvent(FollowTask data, User user) implements INotifyEvent<FollowTask> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.FOLLOW_TASK;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.follow_task_add.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.follow_task_add.content");
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.follow_task_add.title");
    }

    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        return langHelper.get("mail.follow_task_add.content");
    }
}
