package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 走失宠物报备状态
 */
public enum LostPetStatus {
    SEARCHING, // 寻找中
    CLAIMING, // 认领中
    CLAIMED, // 已找到
    CLOSED; // 已关闭

    public static LostPetStatus get(String name) {
        try {
            return LostPetStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.lost_pet_status");
        }
    }
}
