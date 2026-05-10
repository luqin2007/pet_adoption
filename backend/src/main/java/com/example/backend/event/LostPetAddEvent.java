package com.example.backend.event;

import com.example.backend.entity.Location;
import com.example.backend.entity.LostPet;
import com.example.backend.entity.User;

/**
 * 新增走失宠物报备
 * - 触发走失宠物比对流程：检查走失宠物状态，若仍在寻找中则查找可能匹配的流浪宠物并向报备人发送站内信
 *
 * @see com.example.backend.service.LostPetService#addLostPet(com.example.backend.dto.LostPetAddRequest)
 */
public record LostPetAddEvent(LostPet data, User user, Location location) implements IEvent<LostPet> {
}
