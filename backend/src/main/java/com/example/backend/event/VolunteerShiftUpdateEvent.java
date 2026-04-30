package com.example.backend.event;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.VolunteerTask;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

/**
 * 更新志愿者排班
 * - 向被安排的志愿者发送站内信：同步最新排班安排
 * - 向被安排的志愿者发送邮件：同步最新排班安排
 *
 * @see com.example.backend.service.VolunteerService#updateShift(Long, com.example.backend.dto.VolunteerShiftUpdateRequest)
 */
public record VolunteerShiftUpdateEvent(VolunteerShift data, VolunteerTask task, User user) implements INotifyEvent<VolunteerShift> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.VOLUNTEER;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.volunteer_shift_update.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.volunteer_shift_update.content", task.getTitle());
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.volunteer_shift_update.title");
    }

    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        return langHelper.get("mail.volunteer_shift_update.content", task.getTitle());
    }
}
