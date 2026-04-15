package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

public enum AgreementUpdateStatus {
    START, // 开始
    SUCCESS; // 完成

    public static AgreementUpdateStatus get(String name) {
        try {
            return AgreementUpdateStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.status");
        }
    }
}
