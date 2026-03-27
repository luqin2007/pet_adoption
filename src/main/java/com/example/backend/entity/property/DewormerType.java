package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 驱虫药类型
 */
public enum DewormerType {
    INTERNAL, // 内驱
    EXTERNAL, // 外驱
    OTHER; // 其他

    public static DewormerType get(String name) {
        try {
            return DewormerType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("无效驱虫药类型 " + name);
        }
    }
}
