package com.example.backend.event;

import com.example.backend.entity.Location;
import com.example.backend.entity.RescueTask;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

/**
 * 创建救助任务
 * - 向工作人员发送站内信：提醒审核新的救助任务
 *
 * @see com.example.backend.service.RescueTaskService#addRescueTask(com.example.backend.dto.RescueTaskAddRequest)
 */
public record RescueTaskAddEvent(RescueTask data, Location location, User user) implements INotifyEvent<RescueTask> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.RESCUE_TASK;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.rescue_task_add.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.rescue_task_add.content", data.getSummary());
    }
}
