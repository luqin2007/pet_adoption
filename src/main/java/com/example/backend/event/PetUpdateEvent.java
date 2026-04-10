package com.example.backend.event;

import com.example.backend.entity.Pet;

/**
 * 宠物状态变更事件
 * - 重新进行认领匹配
 */
public record PetUpdateEvent(Pet pet) {
    // TODO
}
