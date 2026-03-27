package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.service.UserService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户管理模块<br>
 * - 用户注册 ( √ × )<br>
 *   - 注册：register ( √ × )<br>
 *   - 检查用户名重复：isUsernameExist ( √ × )<br>
 *   - 检查邮箱重复：isMailExist ( √ × )<br>
 *   - 发送邮箱验证码：sendMailCode ( √ × )<br>
 * - 用户登录 ( √ × )<br>
 *   - 登录：login ( √ × )<br>
 *   - 获取用户信息：getUser ( √ × )<br>
 * - 找回密码 ( √ × )<br>
 *   - 忘记密码：forgetPassword ( √ × )<br>
 *   - 重置密码：resetPassword ( √ × )<br>
 * - 信息维护 ( √ × )<br>
 *   - 修改用户信息：updateUser ( √ × )<br>
 *   - 上传头像：uploadAvatar ( √ × )<br>
 *   - 删除头像：deleteAvatar ( √ × )<br>
 * - 账号状态管理 ( √ × )<br>
 *   - 获取用户列表：getUserList ( √ × )<br>
 *   - 删除用户：deleteUser ( √ × )
 */
@Validated
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserResponse> register(UserRegisterTable user) {
        UserResponse response = userService.register(user);
        return Result.success(response);
    }

    /**
     * 注册页面，检查用户名是否存在
     */
    @GetMapping("/check/username/{username}")
    public Result<Void> isUsernameExist(@PathVariable String username) {
        if (userService.isUsernameExist(username)) {
            return Result.error(500, "用户名已存在");
        }
        return Result.success();
    }

    /**
     * 注册页面，检查邮箱是否存在
     */
    @GetMapping("/check/email/{email}")
    public Result<Void> isMailExist(@PathVariable String email) {
        if (userService.isEmailExist(email)) {
            return Result.error(500, "邮箱已存在");
        }
        return Result.success();
    }

    /**
     * 发送邮箱验证码
     */
    @GetMapping("/check/code")
    public Result<Void> sendMailCode(@Email(message = "邮箱格式错误") @RequestParam("email") String email) {
        userService.sendMailCode(email);
        return Result.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserResponse> login(@RequestBody UserLoginRequest user) {
        UserResponse response = userService.login(user.getUsername(), user.getPassword());
        return Result.success(response);
    }

    /**
     * 忘记密码 - 发送密码重置链接
     */
    @GetMapping("/forget")
    public Result<Void> forgetPassword(@Email(message = "邮箱格式错误") @RequestParam("email") String email) {
        userService.forgetPassword(email);
        return Result.success();
    }

    /**
     * 忘记密码 - 重置密码
     */
    @PostMapping("/reset")
    public Result<Void> resetPassword(@RequestBody PasswordResetRequest request) {
        userService.resetPassword(request);
        return Result.success();
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/users/{id}")
    public Result<UserResponse> getUser(@PathVariable("id") Long userId) {
        UserResponse response = userService.getUser(userId);
        return Result.success(response);
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/users/{id}")
    public Result<UserResponse> updateUser(@PathVariable("id") Long userId, @RequestBody UserUpdateRequest user) {
        UserResponse response = userService.update(userId, user);
        return Result.success(response);
    }

    /**
     * 上传头像
     */
    @PostMapping("/users/{id}/avatar")
    public Result<String> uploadAvatar(@PathVariable("id") Long userId, MultipartFile file) {
        String response = userService.uploadAvatar(userId, file);
        return Result.success(response);
    }

    /**
     * 删除头像
     */
    @DeleteMapping("/users/{id}/avatar")
    public Result<Void> deleteAvatar(@PathVariable("id") Long userId) {
        userService.deleteAvatar(userId);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> removeUser(@PathVariable("id") Long userId) {
        userService.removeUser(userId);
        return Result.success();
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public Result<Page<UserResponse>> getUserList(PageParams pageParams) {
        Page<UserResponse> response = userService.getAllUsers(pageParams);
        return Result.success(response);
    }

}
