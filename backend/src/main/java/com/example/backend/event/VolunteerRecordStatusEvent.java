package com.example.backend.event;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerServiceRecord;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.entity.property.VolunteerRecordStatus;
import com.example.backend.util.LangHelper;
import com.example.backend.util.StringUtils;

/**
 * 志愿者服务记录状态变更
 * - 向工作人员发送站内信：同步志愿者提交的服务记录
 * - 向志愿者发送站内信：同步服务记录审核结果
 * - 向志愿者发送邮件：同步服务记录审核结果
 *
 * @see com.example.backend.service.VolunteerService#updateServiceRecordStatus(Long, com.example.backend.dto.VolunteerRecordReviewRequest)
 */
public record VolunteerRecordStatusEvent(VolunteerServiceRecord data, User user, VolunteerRecordStatus oldStatus) implements IReviewEvent<VolunteerServiceRecord> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.VOLUNTEER;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return data.getStatus() == VolunteerRecordStatus.SUBMITTED
                ? langHelper.get("notification.volunteer_record_submit.title")
                : langHelper.get("notification.volunteer_record_review.title");
    }

    /**
     * @param args shiftTitle，仅在提交服务记录、但事件本身未携带排班标题时传入
     */
    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        if (data.getStatus() == VolunteerRecordStatus.SUBMITTED) {
            return langHelper.get("notification.volunteer_record_submit.content", args[0]);
        }
        String statusText = volunteerRecordStatus(data.getStatus());
        String key = StringUtils.hasText(data.getReviewComment())
                ? "notification.volunteer_record_review.content1"
                : "notification.volunteer_record_review.content0";
        return langHelper.get(key, statusText, data.getReviewComment());
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.volunteer_record_review.title");
    }

    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        String statusText = volunteerRecordStatus(data.getStatus());
        String key = StringUtils.hasText(data.getReviewComment())
                ? "mail.volunteer_record_review.content1"
                : "mail.volunteer_record_review.content0";
        return langHelper.get(key, statusText, data.getReviewComment());
    }

    @Override
    public Long applicantId() {
        return data.getVolunteerId();
    }

    @Override
    public Long reviewerId() {
        return data.getReviewerId();
    }

    private String volunteerRecordStatus(VolunteerRecordStatus status) {
        return switch (status) {
            case DRAFT -> "草稿";
            case SUBMITTED -> "已提交";
            case APPROVED -> "审核通过";
            case REJECTED -> "审核拒绝";
        };
    }
}
