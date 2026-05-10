package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 宠物状态
 */
public enum PetStatus {
    WAITING, // 待审核
    AGAINST, // 审核未通过
    FINDING, // 已审核，查找中
    DIED, // 已找到，无法救助或已死亡
    TIMEOUT, // 已超时，放弃救助
    SHELTERED, // 已收容
    HEALTH, // 已完成体检，可领养
    ADOPTED, // 已领养
    HOME; // 丢失宠物，已找到

    public boolean isAdoptable() {
        return this == SHELTERED || this == HEALTH;
    }

    public static PetStatus get(String name) {
        try {
            return PetStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.status");
        }
    }
}
