package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.entity.User;
import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtils;
import com.example.backend.util.PageUtils;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理模块
 * - 用户注册 ( √ × )
 *   - 注册：register ( √ × )
 *   - 检查用户名重复：isUsernameExist ( √ × )
 *   - 检查邮箱重复：isMailExist ( √ × )
 *   - 发送邮箱验证码：sendMailCode ( √ × )
 * - 用户登录 ( √ × )
 *   - 登录：login ( √ × )
 *   - 获取用户信息：getUser ( √ × )
 * - 找回密码 ( √ × )
 *   - 忘记密码：forgetPassword ( √ × )
 *   - 重置密码：resetPassword ( √ × )
 * - 信息维护 ( √ × )
 *   - 修改用户信息：updateUser ( √ × )
 * - 账号状态管理 ( √ × )
 *   - 获取用户列表：getUserList ( √ × )
 *   - 删除用户：deleteUser ( √ × )
 */
@Validated
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserResponse> register(@RequestBody UserRegisterRequest user) {
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
        UserResponse response = userService.login(user);
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
        UserResponse response = userService.getUser(userId, false);
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
    public Result<Page<UserResponse>> getUserList(@RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(defaultValue = "id") String sort,
                                                  @RequestParam(defaultValue = "ASC") String order) {
        Page<User> page = PageUtils.createPage(current, size, sort, order);
        Page<UserResponse> response = userService.getAllUsers(page);
        return Result.success(response);
    }

}
