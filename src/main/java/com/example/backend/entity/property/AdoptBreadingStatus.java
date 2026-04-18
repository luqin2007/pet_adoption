package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 领养/寄养流程状态
 */
public enum AdoptBreadingStatus {
    CREATE("已提交"), // 已提交申请
    PASS("审核通过"), // 审核通过
    REJECT("审核拒绝"), // 审核拒绝
    AGREEMENT_DRAFT("协议草拟中"), // 协议草拟中
    AGREEMENT_SIGNED("协议已签署"), // 协议已签订
    TRACKING("回访中"), // 跟踪期
    FINISH("流程完成"), // 流程完结
    CANCEL("已取消"); // 取消

    public final String name;

    AdoptBreadingStatus(String name) {
        this.name = name;
    }

    public static AdoptBreadingStatus get(String name) {
        try {
            return AdoptBreadingStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.adopt_breading_status");
        }
    }

    public boolean isChangeable() {
        return this != CANCEL && this != FINISH;
    }
}

