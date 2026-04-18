package com.example.backend.event;

import com.example.backend.entity.PetStatusRecord;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;
import com.example.backend.util.NoticeTextUtils;

/**
 * 宠物状态变化
 * - 通知管理员：信息更新
 *
 * @see com.example.backend.service.PetService#updateStatus(Long, com.example.backend.dto.PetStatusUpdateRequest)
 */
public record PetStatusChangeEvent(PetStatusRecord data, User user) implements INotifyEvent<PetStatusRecord> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.PET_RECORD;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.pet_status.title");
    }

    /**
     * @param args petName
     */
    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.pet_status.content", args[0], NoticeTextUtils.petStatus(data.getTo()));
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.pet_status.title");
    }

    /**
     * @param args petName
     */
    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        return langHelper.get("mail.pet_status.content", args[0], NoticeTextUtils.petStatus(data.getTo()));
    }
}
