package com.example.backend.event;

import com.example.backend.entity.Breading;

/**
 * 申请寄养
 * - 通知管理员审核
 */
public record BreadingAddEvent(Breading breading) {
}
