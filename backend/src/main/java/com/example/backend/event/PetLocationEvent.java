package com.example.backend.event;

import com.example.backend.entity.Location;
import com.example.backend.entity.User;

/**
 * 修改流浪宠物位置
 * - 触发走失宠物比对流程：检查流浪宠物状态，若可参与比对则根据最新位置重新查找可能匹配的走失宠物并向报备人发送站内信
 *
 * @see com.example.backend.service.PetService#addLocation(Long, com.example.backend.dto.LocationRequest)
 */
public record PetLocationEvent(Location data, User user) implements IEvent<Location> {
}
