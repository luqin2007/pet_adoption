package com.example.backend.event;

import com.example.backend.entity.Agreement;
import com.example.backend.entity.AgreementFile;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.LangHelper;

import java.util.List;

/**
 * 创建协议
 * - 通知申请人：提醒查看新创建的协议
 *
 * @see com.example.backend.service.AdoptBreadingService#addAgreement(com.example.backend.dto.AgreementAddRequest)
 */
public record AgreementAddEvent(Agreement data, List<AgreementFile> files, User user) implements INotifyEvent<Agreement> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.AGREEMENT;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.agreement_add.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        String parentTypeName = data.getParentType() == ParentType.ADOPT ? "领养" : "寄养";
        return langHelper.get("notification.agreement_add.content", parentTypeName, data.getType().name);
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.agreement_add.title");
    }

    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        String parentTypeName = data.getParentType() == ParentType.ADOPT ? "领养" : "寄养";
        return langHelper.get("mail.agreement_add.content", parentTypeName, data.getType().name);
    }
}
