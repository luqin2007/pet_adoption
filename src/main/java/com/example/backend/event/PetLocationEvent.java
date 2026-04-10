package com.example.backend.event;

import com.example.backend.entity.Location;

/**
 * 添加宠物位置
 * - 重新检查领养宠物
 */
public record PetLocationEvent(Location location) {
}
