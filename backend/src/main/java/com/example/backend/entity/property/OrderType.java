package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 处方类型
 */
public enum OrderType {
    MEDICINE, // 药品
    SURGERY, // 手术
    EXAMINATION, // 检查
    OTHER; // 其他

    public static OrderType get(String name) {
        try {
            return OrderType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.order_type");
        }
    }
}
