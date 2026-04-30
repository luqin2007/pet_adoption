package com.example.backend.event;

import com.example.backend.entity.LostPetClaim;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;

/**
 * 申请认领事件
 * - 向工作人员发送站内信：提醒审核新的认领申请
 * - 向走失宠物发布者发送站内信：同步已有新的认领申请
 *
 * @see com.example.backend.service.LostPetService#addClaim(com.example.backend.dto.LostPetClaimAddRequest)
 */
public record LostPetClaimAddEvent(LostPetClaim data, User user) implements INotifyEvent<LostPetClaim> {

    @Override
    public NoticeSource getSource() {
        return NoticeSource.PET_CLAIM;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.lost_pet_claim_add.title");
    }

    /**
     * args[0]: lostPet.name
     */
    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        return langHelper.get("notification.lost_pet_claim_owner.content", args[0]);
    }
}
