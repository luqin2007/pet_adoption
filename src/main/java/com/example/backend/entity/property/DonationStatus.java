package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;

public enum DonationStatus {
    CREATED("已创建"), // 创建
    PENDING("待处理", CREATED), // 等待处理
    TRANSFERRING("运输中", CREATED, PENDING), // 运输中
    RECEIVED("已接收", CREATED, PENDING, TRANSFERRING), // 已接收
    STOCKED("已入库", RECEIVED), // 已入库
    REFUSED("已拒绝", CREATED, PENDING, TRANSFERRING, RECEIVED), // 已拒绝
    BACKING("退回中", REFUSED), // 退回中
    CLOSED("已关闭", BACKING), // 已关闭
    CANCELED("已取消", CREATED, PENDING); // 已取消

    public final String name;
    private final EnumSet<DonationStatus> previousStatus;

    DonationStatus(String name, DonationStatus... previous) {
        this.name = name;
        if (previous.length == 0)
            this.previousStatus = EnumSet.noneOf(DonationStatus.class);
        else if (previous.length == 1)
            this.previousStatus = EnumSet.of(previous[0]);
        else
            this.previousStatus = EnumSet.of(previous[0], Arrays.copyOfRange(previous, 1, previous.length));
    }

    public boolean canChangeFrom(DonationStatus status) {
        return this.previousStatus.contains(status);
    }

    public static DonationStatus get(String name) {
        try {
            return DonationStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.donation_status");
        }
    }
}
