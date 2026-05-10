package com.example.backend.event;

import com.example.backend.entity.Adopt;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;
import com.example.backend.util.StringUtils;

/**
 * 领养状态变更事件
 * - 通知申请人：同步领养申请状态变化
 * - 通知审核人：同步领养申请状态变化
 *
 * @see com.example.backend.service.AdoptBreadingService#updateAdoptStatus(Long, String)
 */
public record AdoptStatusEvent(Adopt data, User user) implements IReviewEvent<Adopt> {
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
        return NoticeSource.PET_ADOPT;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.adopt_status.title");
    }

    /**
     * @param args name
     */
    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        String rejectReason = data.getRejectReason();
        String notifyContentKey = StringUtils.hasText(rejectReason)
                ? "notification.adopt_status.content1"
                : "notification.adopt_status.content0";
        return langHelper.get(notifyContentKey, args[0], data.getStatus().name, rejectReason);
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.adopt_status.title");
    }

    /**
     * @param args name
     */
    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        String rejectReason = data.getRejectReason();
        String mailContentKey = StringUtils.hasText(rejectReason)
                ? "mail.adopt_status.content1"
                : "mail.adopt_status.content0";
        return langHelper.get(mailContentKey, args[0], data.getStatus().name, rejectReason);
    }
}
