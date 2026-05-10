package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 认领申请状态
 */
public enum ClaimStatus {
    PENDING("待审核"), // 待审核
    PASS("审核通过"), // 审核通过
    REJECT("审核拒绝"); // 拒绝

    public final String name;

    ClaimStatus(String name) {
        this.name = name;
    }

    public static ClaimStatus get(String name) {
        try {
            return ClaimStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.status");
        }
    }
}
