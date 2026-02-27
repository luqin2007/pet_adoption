package com.example.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.example.backend.dto.*;
import com.example.backend.entity.User;
import com.example.backend.service.UserManagerService;
import com.example.backend.util.JwtUtils;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
public class UserManagerController {

    private final UserManagerService userManagerService;
    private final JwtUtils jwtUtils;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserResponse> register(@RequestBody UserRegisterRequest user) {
        User register = userManagerService.register(user);
        String accessToken = jwtUtils.generateAccessToken(register);
        String refreshToken = jwtUtils.generateRefreshToken(register);
        UserResponse response = UserResponse.fromEntity(register, accessToken, refreshToken);
        return Result.success(response);
    }

    /**
     * 注册页面，检查用户名是否存在
     */
    @GetMapping("/check/username/{username}")
    public Result<Void> isUsernameExist(@PathVariable String username) {
        if (userManagerService.isUsernameExist(username)) {
            return Result.error(500, "用户名已存在");
        }
        return Result.success();
    }

    /**
     * 注册页面，检查邮箱是否存在
     */
    @GetMapping("/check/email/{email}")
    public Result<Void> isMailExist(@PathVariable String email) {
        if (userManagerService.isEmailExist(email)) {
            return Result.error(500, "邮箱已存在");
        }
        return Result.success();
    }

    /**
     * 发送邮箱验证码
     */
    @GetMapping("/check/code")
    public Result<Void> sendMailCode(@Email(message = "邮箱格式错误") @RequestParam("email") String email) {
        userManagerService.sendMailCode(email);
        return Result.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserResponse> login(@RequestBody UserLoginRequest user) {
        User login = userManagerService.login(user);
        String accessToken = jwtUtils.generateAccessToken(login);
        String refreshToken = jwtUtils.generateRefreshToken(login);
        UserResponse response = UserResponse.fromEntity(login, accessToken, refreshToken);
        return Result.success(response);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/users/{id}")
    public Result<UserResponse> getUser(@PathVariable Long id) {
        User user = userManagerService.getUser(id);
        UserResponse response = UserResponse.fromEntity(user);
        return Result.success(response);
    }

    /**
     * 忘记密码 - 发送密码重置链接
     */
    @GetMapping("/forget")
    public Result<Void> forgetPassword(@Email(message = "邮箱格式错误") @RequestParam("email") String email) {
        userManagerService.forgetPassword(email);
        return Result.success();
    }

    /**
     * 忘记密码 - 重置密码
     */
    @PostMapping("/reset")
    public Result<Void> resetPassword(@RequestBody PasswordResetRequest request) {
        userManagerService.resetPassword(request);
        return Result.success();
    }

    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public Result<PageDTO<UserResponse>> getUserList(@RequestParam(defaultValue = "1") Integer current,
                                                     @RequestParam(defaultValue = "10") Integer size,
                                                     @RequestParam(defaultValue = "id") String sort,
                                                     @RequestParam(defaultValue = "ASC") String order) {
        // 分页
        List<OrderItem> orders = order.equalsIgnoreCase("ASC")
                ? OrderItem.ascs(sort)
                : OrderItem.descs(sort);
        Page<User> page = Page.of(current, size);
        page.setOrders(orders);

        // 查询
        IPage<User> users = userManagerService.getAllUsers(page);
        List<UserResponse> records = users.getRecords().stream()
                .map(UserResponse::fromEntity)
                .toList();
        IPage<UserResponse> response =
                PageDTO.of(users.getCurrent(), users.getSize(), users.getTotal());
        // Mybatis-plus 为什么 PageDTO 要求传入 List<Object> 而不是 List<T> ?
        response.setRecords(records);
        return Result.success((PageDTO<UserResponse>) response);
    }

    /**
     * 更新用户信息
     */
    @PostMapping("/users/{id}")
    public Result<UserResponse> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest user) {
        User update = userManagerService.update(id, user);
        UserResponse response = UserResponse.fromEntity(update);
        return Result.success(response);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    public Result<Void> removeUser(@PathVariable Long id) {
        userManagerService.removeUser(id);
        return Result.success();
    }
}
