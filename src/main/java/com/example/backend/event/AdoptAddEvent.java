package com.example.backend.event;

import com.example.backend.entity.Adopt;

/**
 * 申请领养
 * - 通知管理员审核
 */
public record AdoptAddEvent(Adopt adopt) {
}
