package com.example.backend.event;

import com.example.backend.entity.Location;
import com.example.backend.entity.RescueTask;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

/**
 * 更新救助任务
 * - 通知发起者：同步救助任务信息变更
 * - 通知参与者：同步救助任务信息变更
 *
 * @see com.example.backend.service.RescueTaskService#updateRescueTask(Long, com.example.backend.dto.RescueTaskUpdateRequest)
 */
public record RescueTaskUpdateEvent(RescueTask data, Location location, User user) implements INotifyEvent<RescueTask> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.RESCUE_TASK;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.rescue_task_update.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.rescue_task_update.content", data.getSummary());
    }
}
