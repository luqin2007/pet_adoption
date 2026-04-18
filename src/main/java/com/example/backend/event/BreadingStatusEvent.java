package com.example.backend.event;

import com.example.backend.entity.Breading;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;
import com.example.backend.util.StringUtils;

/**
 * 寄养状态变更事件
 * - 通知申请人：同步寄养申请状态
 * - 通知审核人：同步寄养申请状态
 *
 * @see com.example.backend.service.AdoptBreadingService#updateBreadingStatus(Long, String)
 */
public record BreadingStatusEvent(Breading data, User user) implements IReviewEvent<Breading> {
    @Override
    public Long applicantId() {
        return data.getApplicantId();
    }

    @Override
    public Long reviewerId() {
        return data.getReviewerId();
    }

    @Override
    public NoticeSource getSource() {
        return NoticeSource.BREADING;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.breading_status.title");
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.breading_status.title");
    }

    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        String rejectReason = data.getRejectReason();
        String notifyContentKey = StringUtils.hasText(rejectReason)
                ? "mail.breading_status.content1"
                : "mail.breading_status.content0";
        return langHelper.get(notifyContentKey,
                data.getPetName(), data.getStatus().name, rejectReason);
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        String rejectReason = data.getRejectReason();
        String notifyContentKey = StringUtils.hasText(rejectReason)
                ? "notification.breading_status.content1"
                : "notification.breading_status.content0";
        return langHelper.get(notifyContentKey,
                data.getPetName(), data.getStatus().name, rejectReason);
    }
}
