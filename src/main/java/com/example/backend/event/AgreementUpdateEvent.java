package com.example.backend.event;

import com.example.backend.entity.Agreement;
import com.example.backend.entity.AgreementUpdateRecord;
import com.example.backend.entity.User;

/**
 * 创建/修改协议
 */
public record AgreementUpdateEvent(Agreement agreement, AgreementUpdateRecord record, User user) {
}
