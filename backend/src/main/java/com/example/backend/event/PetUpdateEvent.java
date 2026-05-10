package com.example.backend.event;

import com.example.backend.entity.Pet;
import com.example.backend.entity.User;

/**
 * 修改流浪宠物信息/特征
 * - 触发走失宠物比对流程：检查流浪宠物状态，若可参与比对则根据最新信息重新查找可能匹配的走失宠物并向报备人发送站内信
 *
 * @see com.example.backend.service.PetService#updatePet(Long, com.example.backend.dto.PetUpdateRequest)
 * @see com.example.backend.service.PetService#addTags(Long, com.example.backend.dto.PetTagAddRequest)
 * @see com.example.backend.service.PetService#deleteTags(Long, com.example.backend.dto.IdsRequest)
 */
public record PetUpdateEvent(Pet data, User user) implements IEvent<Pet> {
}
