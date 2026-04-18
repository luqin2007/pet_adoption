package com.example.backend.event;

import com.example.backend.entity.RescueTask;
import com.example.backend.entity.RescueTaskRecord;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;
import com.example.backend.util.NoticeTextUtils;
import com.example.backend.util.StringUtils;

/**
 * 更新救助任务状态
 * - 通知发起者：同步救助任务状态变化
 * - 通知参与者：同步救助任务状态变化
 *
 * @see com.example.backend.service.RescueTaskService#updateRescueTaskStatus(Long, com.example.backend.dto.RescueTaskRecordStatusUpdateRequest)
 */
public record RescueTaskStatusEvent(RescueTask data, RescueTaskRecord record, User user) implements INotifyEvent<RescueTask> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.RESCUE_TASK;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.rescue_task_status.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        String key = StringUtils.hasText(record.getReason())
                ? "notification.rescue_task_status.content0"
                : "notification.rescue_task_status.content1";
        return langHelper.get(key, data.getSummary(), NoticeTextUtils.rescueTaskStatus(record.getStatusTo()), record.getReason());
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.rescue_task_status.title");
    }

    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        String key = StringUtils.hasText(record.getReason())
                ? "mail.rescue_task_status_with_reason.content"
                : "mail.rescue_task_status.content";
        return langHelper.get(key, data.getSummary(), NoticeTextUtils.rescueTaskStatus(record.getStatusTo()), record.getReason());
    }
}
