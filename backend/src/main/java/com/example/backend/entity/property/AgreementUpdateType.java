package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 协议更新类型
 */
public enum AgreementUpdateType {

    CREATE, // 创建
    DISCARD, // 删除
    UPLOAD, // 上传图片
    UPDATE, // 更新内容
    SIGN_UPLOAD, // 上传签名
    SIGN_CONFIRM, // 确认签署
    SIGN_REFUSE, // 拒绝签署
    SIGN; // 签订

    public static AgreementUpdateType get(String name) {
        try {
            return AgreementUpdateType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.agreement_update_type");
        }
    }
}
