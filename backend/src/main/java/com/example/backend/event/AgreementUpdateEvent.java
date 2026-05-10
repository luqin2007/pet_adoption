package com.example.backend.event;

import com.example.backend.entity.Agreement;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.LangHelper;

/**
 * 创建/修改协议
 * - 通知申请人：同步协议内容更新
 * - 通知审核人：提醒查看协议最新版本
 *
 * @see com.example.backend.service.AdoptBreadingService#updateAgreement(Long, com.example.backend.dto.AgreementUpdateRequest)
 * @see com.example.backend.service.AdoptBreadingService#uploadAgreement(Long, com.example.backend.dto.AgreementFilesUploadTable)
 * @see com.example.backend.service.AdoptBreadingService#signAgreement(Long, org.springframework.web.multipart.MultipartFile)
 */
public record AgreementUpdateEvent(Agreement data, User user) implements INotifyEvent<Agreement> {
    @Override
    public NoticeSource getSource() {
        return NoticeSource.AGREEMENT;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.agreement_update.title");
    }

    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        String parentTypeName = data.getParentType() == ParentType.ADOPT ? "领养" : "寄养";
        return langHelper.get("notification.agreement_update.content", parentTypeName);
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.agreement_update.title");
    }

    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        String parentTypeName = data.getParentType() == ParentType.ADOPT ? "领养" : "寄养";
        return langHelper.get("mail.agreement_update.content", parentTypeName);
    }
}
