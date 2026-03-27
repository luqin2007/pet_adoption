package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 诊疗流程状态
 */
public enum MedicalRecordStatus {
    WAITING, // 待接诊
    PROCESSING, // 接诊中
    PAYING, // 待缴费
    COMPLETED, // 完成
    CANCELED; // 取消

    public static MedicalRecordStatus get(String name) {
        try {
            return MedicalRecordStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("无效就诊状态 " + name);
        }
    }
}
