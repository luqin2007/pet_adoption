package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 领养/寄养流程状态
 */
public enum AdoptBreadingStatus {
    CREATE, // 已提交申请
    PASS, // 审核通过
    REJECT, // 审核拒绝
    AGREEMENT_DRAFT, // 协议草拟中
    AGREEMENT_SIGNED, // 协议已签订
    TRACKING, // 跟踪期
    FINISH, // 流程完结
    CANCEL; // 取消

    public static AdoptBreadingStatus get(String name) {
        try {
            return AdoptBreadingStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("无效申请状态 " + name);
        }
    }

    public boolean isChangeable() {
        return this != CANCEL && this != FINISH;
    }
}

