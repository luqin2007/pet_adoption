package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.EnumMap;
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
    private static final EnumMap<DonationStatus, EnumSet<DonationStatus>> PREVIOUS_STATUSES;

    static {
        PREVIOUS_STATUSES = new EnumMap<>(DonationStatus.class);
        PREVIOUS_STATUSES.put(CREATED, EnumSet.noneOf(DonationStatus.class));
        PREVIOUS_STATUSES.put(PENDING, EnumSet.of(CREATED));
        PREVIOUS_STATUSES.put(TRANSFERRING, EnumSet.of(CREATED, PENDING));
        PREVIOUS_STATUSES.put(RECEIVED, EnumSet.of(CREATED, PENDING, TRANSFERRING));
        PREVIOUS_STATUSES.put(STOCKED, EnumSet.of(RECEIVED));
        PREVIOUS_STATUSES.put(REFUSED, EnumSet.of(CREATED, PENDING, TRANSFERRING, RECEIVED));
        PREVIOUS_STATUSES.put(BACKING, EnumSet.of(REFUSED));
        PREVIOUS_STATUSES.put(CLOSED, EnumSet.of(BACKING));
        PREVIOUS_STATUSES.put(CANCELED, EnumSet.of(CREATED, PENDING));
    }

    DonationStatus(String name, DonationStatus... previous) {
        this.name = name;
    }

    public boolean canChangeFrom(DonationStatus status) {
        return PREVIOUS_STATUSES.get(this).contains(status);
    }

    public static DonationStatus get(String name) {
        try {
            return DonationStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.donation_status");
        }
    }
}
