package com.example.backend.event;

import com.example.backend.entity.LostPetClaim;
import com.example.backend.entity.User;
import com.example.backend.entity.property.NoticeSource;
import com.example.backend.util.LangHelper;
import com.example.backend.util.StringUtils;

/**
 * 同意/拒绝认领
 * - 通知申请人：申请结果
 *
 * @see com.example.backend.service.LostPetService#approveClaim(Long, com.example.backend.dto.ClaimApproveRequest)
 */
public record LostPetClaimApproveEvent(LostPetClaim data, User user) implements INotifyEvent<LostPetClaim> {

    @Override
    public NoticeSource getSource() {
        return NoticeSource.PET_CLAIM;
    }

    @Override
    public String buildNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.lost_pet_claim_result.title");
    }

    /**
     * args[0]: lostPet.name
     */
    @Override
    public String buildNotifyContent(LangHelper langHelper, Object... args) {
        String reason = data.getApproveReason();
        String key = StringUtils.hasText(reason)
                ? "notification.lost_pet_claim_result.content1"
                : "notification.lost_pet_claim_result.content0";
        return langHelper.get(key, args[0], data.getStatus().name, reason);
    }

    @Override
    public String buildMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.lost_pet_claim_result.title");
    }

    /**
     * args[0]: lostPet.name
     */
    @Override
    public String buildMailContent(LangHelper langHelper, Object... args) {
        String reason = data.getApproveReason();
        String key = StringUtils.hasText(reason)
                ? "mail.lost_pet_claim_result.content1"
                : "mail.lost_pet_claim_result.content0";
        return langHelper.get(key, args[0], data.getStatus().name, reason);
    }
}
