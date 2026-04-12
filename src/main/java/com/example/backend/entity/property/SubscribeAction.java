package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 订阅类型
 */
public enum SubscribeAction {
    ITEM_CHANGE, // 数量变动时通知
    ITEM_COUNT, // 物品不足时通知
    IN_STOCK, // 入库时通知
    OUT_STOCK, // 出库时通知
    CATEGORY_COUNT, // 某类型物品不足时通知
    DONATE; // 自己捐赠的物资

    public boolean requireId() {
        return this == ITEM_COUNT || this == ITEM_CHANGE || this == CATEGORY_COUNT || this == DONATE;
    }

    public boolean requireCount() {
        return this == ITEM_COUNT || this == CATEGORY_COUNT;
    }

    public boolean bindItem() {
        return this == ITEM_CHANGE || this == ITEM_COUNT;
    }

    public boolean bindCategory() {
        return this == CATEGORY_COUNT;
    }

    public boolean bindStock() {
        return this == IN_STOCK || this == OUT_STOCK;
    }

    public boolean bindUser() {
        return this == DONATE;
    }

    public static SubscribeAction get(String name) {
        try {
            return SubscribeAction.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.subscribe_action");
        }
    }
}
