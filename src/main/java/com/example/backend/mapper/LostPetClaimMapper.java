package com.example.backend.mapper;

import com.example.backend.dto.ClaimQueryParams;
import com.example.backend.entity.LostPetClaim;
import com.example.backend.entity.property.ClaimStatus;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * 走失宠物认领申请 Mapper
 */
@Mapper
public interface LostPetClaimMapper extends IBaseMapper<LostPetClaim> {

    default MPLambdaQuery<LostPetClaim> queryByRequest(ClaimQueryParams params) {
        return lambdaQuery()
                .in(LostPetClaim::getLostPetId, params.getLostPet())
                .in(LostPetClaim::getApplicantId, params.getUser())
                .in(LostPetClaim::getStatus, ClaimStatus::get, params.getStatus())
                .in(LostPetClaim::getCreateTime, params.getTime0(), params.getTime1());
    }

    default MPLambdaUpdate<LostPetClaim> updateStatus(Long id, ClaimStatus status) {
        Date now = new Date();
        return lambdaUpdate()
                .eq(LostPetClaim::getId, id)
                .set(LostPetClaim::getStatus, status)
                .set(LostPetClaim::getReviewTime, now)
                .set(LostPetClaim::getUpdateTime, now);
    }

    default MPLambdaUpdate<LostPetClaim> approve(Long id, ClaimStatus status, String reason) {
        Date now = new Date();
        return lambdaUpdate()
                .eq(LostPetClaim::getId, id)
                .set(LostPetClaim::getStatus, status)
                .set(LostPetClaim::getApproveReason, reason)
                .set(status == ClaimStatus.PASS, LostPetClaim::getClaimTime, now)
                .set(LostPetClaim::getReviewTime, now)
                .set(LostPetClaim::getUpdateTime, now);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.lost_pet_claim";
    }

    @Override
    default Class<LostPetClaim> getEntityClass() {
        return LostPetClaim.class;
    }
}

