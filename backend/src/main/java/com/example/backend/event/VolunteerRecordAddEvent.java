package com.example.backend.event;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerServiceRecord;
import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.VolunteerTask;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

/**
 * 新增志愿者服务记录
 * - 向工作人员发送站内信：提醒审核新的志愿者服务记录
 *
 * @see com.example.backend.service.VolunteerService#addServiceRecord(Long, com.example.backend.dto.VolunteerServiceRecordAddRequest)
 */
public record VolunteerRecordAddEvent(VolunteerShift shift, VolunteerTask task, VolunteerServiceRecord data, User user) implements INotifyEvent<VolunteerServiceRecord> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.VOLUNTEER;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.volunteer_record_add.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.volunteer_record_add.content", task.getTitle());
    }
}
