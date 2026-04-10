package com.example.backend.event;

import com.example.backend.entity.PetStatusRecord;

/**
 * 宠物状态变化
 * - 审核通过，检查丢失宠物
 */
public record PetStatusChangeEvent(PetStatusRecord record) {
}
