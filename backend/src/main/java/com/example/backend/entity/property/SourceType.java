package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

public enum SourceType {
    PURCHASE, // 采购
    DONATION; // 捐赠

    public static SourceType get(String name) {
        try {
            return SourceType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.source_type");
        }
    }
}
