package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

public enum DeliveryType {
    FACE, // 到店面交
    EXPRESS, // 快递
    ADDRESS, // 定点取货
    OTHER; // 其他

    public static DeliveryType get(String name) {
        try {
            return DeliveryType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.delivery_type");
        }
    }
}
