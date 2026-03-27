package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 康复计划状态
 */
public enum RehabPlanStatus {
    ACTIVE, // 生效中
    DISCARD, // 废弃
    COMPLETED; // 已完成

    public static RehabPlanStatus get(String name) {
        try {
            return RehabPlanStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("无效康复状态 " + name);
        }
    }
}