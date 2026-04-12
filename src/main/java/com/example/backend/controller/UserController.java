package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.PageParams;
import com.example.backend.dto.Result;
import com.example.backend.dto.UserResponse;
import com.example.backend.dto.UserUpdateRequest;
import com.example.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理模块<br>
 * - 信息维护 ( √ × )<br>
 * ---- 修改用户信息：updateUser ( √ × )<br>
 * ---- 上传头像：uploadAvatar ( √ × )<br>
 * ---- 删除头像：deleteAvatar ( √ × )<br>
 * - 账号状态管理 ( √ × )<br>
 * ---- 获取用户列表：getUserList ( √ × )<br>
 * ---- 删除用户：deleteUser ( √ × )
 */
@Validated
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public Result<UserResponse> getUser(@PathVariable("id") Long userId) {
        UserResponse response = userService.getUser(userId);
        return Result.success(response);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<UserResponse> updateUser(@PathVariable("id") Long userId,
                                           @Valid @RequestBody UserUpdateRequest user) {
        UserResponse response = userService.update(userId, user);
        return Result.success(response);
    }

    /**
     * 上传头像
     */
    @PatchMapping("/{id}/avatar")
    public Result<String> uploadAvatar(@PathVariable("id") Long userId,
                                       @RequestParam("avatar") MultipartFile file) {
        String response = userService.uploadAvatar(userId, file);
        return Result.success(response);
    }

    /**
     * 删除头像
     */
    @DeleteMapping("/{id}/avatar")
    public Result<Void> deleteAvatar(@PathVariable("id") Long userId) {
        userService.deleteAvatar(userId);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> removeUser(@PathVariable("id") Long userId) {
        userService.removeUser(userId);
        return Result.success();
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/")
    public Result<Page<UserResponse>> getUserList(PageParams pageParams) {
        Page<UserResponse> response = userService.getAllUsers(pageParams);
        return Result.success(response);
    }

}
