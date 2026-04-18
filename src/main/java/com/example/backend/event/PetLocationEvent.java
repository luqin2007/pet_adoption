package com.example.backend.event;

import com.example.backend.entity.Location;

/**
 * 添加宠物位置
 * - 预留扩展：后续可在位置变化后追加匹配、提醒等逻辑
 *
 * @see com.example.backend.service.PetService#addLocation(Long, com.example.backend.dto.LocationRequest)
 */
public record PetLocationEvent(Location location) {
}
