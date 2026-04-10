package com.example.backend.event;

import com.example.backend.entity.LostPetClaim;
import com.example.backend.entity.User;

/**
 * 申请认领事件
 * -- 通知管理员审核
 */
public record LostPetClaimAddEvent(LostPetClaim claim, User user) {
}
