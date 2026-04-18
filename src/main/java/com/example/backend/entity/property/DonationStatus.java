package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

public enum DonationStatus {
    CREATED("已创建"), // 创建
    PENDING("待处理"), // 等待处理
    TRANSFERRING("运输中"), // 运输中
    RECEIVED("已接收"), // 已接收
    STOCKED("已入库"), // 已入库
    REFUSED("已拒绝"), // 已拒绝
    BACKING("退回中"), // 退回中
    CLOSED("已关闭"), // 已关闭
    CANCELED("已取消"); // 已取消

    public final String name;

    DonationStatus(String name) {
        this.name = name;
    }

    public static DonationStatus get(String name) {
        try {
            return DonationStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.donation_status");
        }
    }
}
