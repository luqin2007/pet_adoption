package com.example.backend.event;

import com.example.backend.entity.Agreement;
import com.example.backend.entity.AgreementFile;
import com.example.backend.entity.AgreementUpdateRecord;
import com.example.backend.entity.User;

import java.util.List;

/**
 * 创建协议
 */
public record AgreementAddEvent(Agreement agreement, List<AgreementFile> files, User user) {
}
