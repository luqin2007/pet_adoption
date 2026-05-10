package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 诊疗类型
 */
public enum MedicalRecordType {
    FIRST, // 初诊
    REVISIT, // 复诊
    EMERGENCY, // 急诊
    EXAMINATION; // 体检

    public static MedicalRecordType get(String name) {
        try {
            return MedicalRecordType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.medical_record_type");
        }
    }
}
