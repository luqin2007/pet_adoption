package com.example.backend.event;

import com.example.backend.entity.Location;
import com.example.backend.entity.Pet;
import com.example.backend.entity.User;

/**
 * 发现新流浪宠物
 * - 通知管理员校验
 */
public record PetAddEvent(Pet pet, User user, Location location) {
    // TODO
}
