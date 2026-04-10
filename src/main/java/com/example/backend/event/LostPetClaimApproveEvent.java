package com.example.backend.event;

import com.example.backend.entity.LostPetClaim;

/**
 * 同意/拒绝认领
 */
public record LostPetClaimApproveEvent(LostPetClaim claim) {
}
