package com.example.backend.event;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.VolunteerTask;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

/**
 * 创建志愿者排班
 * - 向被安排的志愿者发送站内信：提醒确认新的排班任务
 * - 向被安排的志愿者发送邮件：提醒及时确认新的排班任务
 *
 * @see com.example.backend.service.VolunteerService#addShift(com.example.backend.dto.VolunteerShiftAddRequest)
 */
public record VolunteerShiftAddEvent(VolunteerShift data, VolunteerTask task, User user) implements INotifyEvent<VolunteerShift> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.VOLUNTEER;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.volunteer_shift_add.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.volunteer_shift_add.content", task.getTitle());
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.volunteer_shift_add.title");
    }

    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        return langHelper.get("mail.volunteer_shift_add.content", task.getTitle());
    }
}
