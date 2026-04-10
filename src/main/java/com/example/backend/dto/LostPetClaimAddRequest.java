package com.example.backend.dto;

import com.example.backend.entity.LostPetClaim;
import com.example.backend.entity.property.ClaimStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 认领申请请求
 */
@Data
public class LostPetClaimAddRequest implements IRequest {

    @NotNull(message = "request.pet.id")
    private Long lostPetId;

    @NotBlank(message = "request.phone")
    private String applicantPhone;

    @NotBlank(message = "request.reason")
    private String reason;

    public LostPetClaim create(Long applicantId) {
        Date now = new Date();
        return new LostPetClaim(null,
                lostPetId,
                applicantId,
                applicantPhone,
                null,
                ClaimStatus.PENDING,
                reason,
                null,
                null,
                null,
                now,
                now);
    }
}
