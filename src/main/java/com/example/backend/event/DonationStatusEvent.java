package com.example.backend.event;

import com.example.backend.entity.Donation;
import com.example.backend.entity.DonationStatusUpdateRecord;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;
import com.example.backend.util.StringUtils;

/**
 * 捐赠更新通知
 * - 向捐赠人发送站内信：同步捐赠状态变化
 * - 向捐赠人发送邮件：同步接收、入库、拒绝、退回等重要结果
 *
 * @see com.example.backend.service.ItemDonationService#updateDonationStatus(Long, com.example.backend.dto.DonationStatusUpdateRequest)
 */
public record DonationStatusEvent(Donation data, DonationStatusUpdateRecord record, User user) implements IReviewEvent<Donation> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.DONATION;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.donation_status.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        String key = StringUtils.hasText(record.getReason())
                ? "notification.donation_status.content1"
                : "notification.donation_status.content0";
        return langHelper.get(key, data.getId(), record.getNewStatus().name, record.getReason());
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.donation_status.title");
    }

    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        String key = StringUtils.hasText(record.getReason())
                ? "mail.donation_status.content1"
                : "mail.donation_status.content0";
        return langHelper.get(key, data.getId(), record.getNewStatus().name, record.getReason());
    }

    @Override
    public Long applicantId() {
        return data.getUserId();
    }

    @Override
    public Long reviewerId() {
        return null;
    }
}
