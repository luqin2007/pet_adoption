package com.example.backend.event;

import com.example.backend.entity.Pet;

/**
 * 宠物状态变更事件
 * - 预留扩展：后续可在宠物信息更新后重新进行认领、走失匹配
 *
 * @see com.example.backend.service.PetService#updatePet(Long, com.example.backend.dto.PetUpdateRequest)
 */
public record PetUpdateEvent(Pet pet) {
}
