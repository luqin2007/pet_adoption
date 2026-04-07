package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

public enum DonationStatus {
    CREATED, // 创建
    PENDING, // 等待处理
    TRANSFERRING, // 运输中
    RECEIVED, // 已接收
    STOCKED, // 已入库
    REFUSED, // 已拒绝
    BACKING, // 退回中
    CLOSED, // 已关闭
    CANCELED; // 已取消

    public static DonationStatus get(String name) {
        try {
            return DonationStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("无效驱虫药类型 " + name);
        }
    }
}
