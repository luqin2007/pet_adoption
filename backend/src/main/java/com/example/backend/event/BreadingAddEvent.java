package com.example.backend.event;

import com.example.backend.entity.Breading;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

/**
 * 申请寄养
 * - 通知管理员：审核新的寄养申请
 *
 * @see com.example.backend.service.AdoptBreadingService#addBreading(com.example.backend.dto.BreadingAddRequest)
 */
public record BreadingAddEvent(Breading data, User user) implements INotifyEvent<Breading> {

    @Override
    public NoticeSource getSource() {
        return NoticeSource.BREADING;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.breading_add.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.breading_add.content", data.getPetName());
    }
}
