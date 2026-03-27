package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 医疗检查类型
 */
public enum ExamType {
    BLOOD, // 血常规
    BIOCHEMISTRY, // 生物化学检查
    XRAY, // X 光检查
    ULTRASOUND, // 超声检查
    FECAL_EXAM, // 粪便检查
    URINALYSIS, // 尿检
    INFECTION, // 传染与免疫检查
    CELL, // 细胞与病理学检查
    SEMINAL, // 内分泌
    OTHER; // 其他检查

    public static ExamType get(String name) {
        try {
            return ExamType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("无效检查类型 " + name);
        }
    }
}
