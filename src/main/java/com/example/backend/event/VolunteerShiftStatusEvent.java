package com.example.backend.event;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.VolunteerShiftStatusRecord;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.entity.property.VolunteerShiftStatus;
import com.example.backend.util.LangHelper;
import com.example.backend.util.StringUtils;

/**
 * 志愿者排班状态变更
 * - 向排班人发送站内信：同步志愿者确认排班
 * - 向志愿者发送站内信：同步排班状态变化
 * - 向志愿者发送邮件：同步取消、完成、缺勤等关键状态结果
 *
 * @see com.example.backend.service.VolunteerService#updateShiftStatus(Long, com.example.backend.dto.VolunteerShiftStatusUpdateRequest)
 */
public record VolunteerShiftStatusEvent(VolunteerShift data, VolunteerShiftStatusRecord record, User user) implements IReviewEvent<VolunteerShift> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.VOLUNTEER;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        if (record.getStatusTo() == VolunteerShiftStatus.CONFIRMED) {
            return langHelper.get("notification.volunteer_shift_confirmed.title");
        }
        return langHelper.get("notification.volunteer_shift_status.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        if (record.getStatusTo() == VolunteerShiftStatus.CONFIRMED) {
            return langHelper.get("notification.volunteer_shift_confirmed.content", data.getTitle());
        }
        String statusText = volunteerShiftStatus(record.getStatusTo());
        String key = StringUtils.hasText(record.getComment())
                ? "notification.volunteer_shift_status_with_reason.content"
                : "notification.volunteer_shift_status.content";
        return langHelper.get(key, data.getTitle(), statusText, record.getComment());
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.volunteer_shift_status.title");
    }

    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        String statusText = volunteerShiftStatus(record.getStatusTo());
        String key = StringUtils.hasText(record.getComment())
                ? "mail.volunteer_shift_status_with_reason.content"
                : "mail.volunteer_shift_status.content";
        return langHelper.get(key, data.getTitle(), statusText, record.getComment());
    }

    @Override
    public Long applicantId() {
        return data.getVolunteerId();
    }

    @Override
    public Long reviewerId() {
        return data.getAssignerId();
    }

    private String volunteerShiftStatus(VolunteerShiftStatus status) {
        return switch (status) {
            case ASSIGNED -> "已分配";
            case CONFIRMED -> "已确认";
            case IN_PROGRESS -> "执行中";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
            case ABSENT -> "缺勤";
        };
    }
}
