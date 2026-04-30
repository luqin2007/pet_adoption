package com.example.backend.event;

import com.example.backend.entity.Donation;
import com.example.backend.entity.DonationItem;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

import java.util.List;

/**
 * 捐赠更新通知
 * - 向工作人员发送站内信：提醒捐赠内容已被修改
 *
 * @see com.example.backend.service.ItemDonationService#updateDonation(Long, com.example.backend.dto.DonationUpdateRequest)
 */
public record DonationUpdateEvent(Donation data, List<DonationItem> items, User user) implements INotifyEvent<Donation> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.DONATION;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.donation_update.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.donation_update.content", data.getId());
    }
}
