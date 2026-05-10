package com.example.backend.event;

import com.example.backend.dto.AdoptAddRequest;
import com.example.backend.entity.Adopt;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

/**
 * 申请领养
 * - 通知管理员：审核新的领养申请
 *
 * @see com.example.backend.service.AdoptBreadingService#addAdopt(AdoptAddRequest)
 */
public record AdoptAddEvent(Adopt data, User user) implements INotifyEvent<Adopt> {

    @Override
    public NoticeSource getSource() {
        return NoticeSource.PET_ADOPT;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.adopt_add.title");
    }

    /**
     * @param args name
     */
    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.adopt_add.content", args[0]);
    }
}
