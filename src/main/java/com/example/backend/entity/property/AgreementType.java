package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

public enum AgreementType {

    ELECTRONIC, // 电子协议
    PAPER; // 纸质协议

    public static AgreementType get(String name) {
        try {
            return AgreementType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.agreement_type");
        }
    }
}
