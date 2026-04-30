package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.UserService;
import com.example.backend.util.JwtHelper;
import com.example.backend.util.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 认证相关 API<br>
 * - 用户注册 ( √ × )<br>
 * ---- 注册：register ( √ × )<br>
 * ---- 检查用户名重复：isUsernameExist ( √ × )<br>
 * ---- 检查邮箱重复：isMailExist ( √ × )<br>
 * ---- 发送邮箱验证码：sendMailCode ( √ × )<br>
 * - 用户登录 ( √ × )<br>
 * ---- 登录：login ( √ × )<br>
 * ---- 获取用户信息：getUser ( √ × )<br>
 * - 找回密码 ( √ × )<br>
 * ---- 忘记密码：forgetPassword ( √ × )<br>
 * ---- 重置密码：resetPassword ( √ × )<br>
 * - 刷新认证<br>
 * - 登出
 */
@Validated
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtHelper jwtHelper;
    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserResponse> register(@Valid @ModelAttribute UserRegisterTable user) {
        UserResponse response = userService.register(user);
        return Result.success(response);
    }

    /**
     * 注册页面，检查用户名是否存在
     */
    @GetMapping("/check/username/{username}")
    public Result<Void> isUsernameExist(@PathVariable String username) {
        if (userService.isUsernameExist(username)) {
            return Result.error(ServiceException.E_INVALIDATE, "用户名已存在");
        }
        return Result.success();
    }

    /**
     * 注册页面，检查邮箱是否存在
     */
    @GetMapping("/check/email/{email}")
    public Result<Void> isMailExist(@PathVariable String email) {
        if (userService.isEmailExist(email)) {
            return Result.error(ServiceException.E_INVALIDATE, "邮箱已存在");
        }
        return Result.success();
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/check/code")
    public Result<Void> sendMailCode(@Email(message = "request.user.email")
                                     @RequestParam("email") String email) {
        userService.sendMailCode(email);
        return Result.success();
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserResponse> login(@Valid @RequestBody UserLoginRequest user) {
        UserResponse response = userService.login(user.getUsername(), user.getPassword());
        return Result.success(response);
    }

    /**
     * 忘记密码 - 发送密码重置链接
     */
    @PostMapping("/forget")
    public Result<Void> forgetPassword(@Email(message = "request.user.email")
                                       @RequestParam("email") String email) {
        userService.forgetPassword(email);
        return Result.success();
    }

    /**
     * 忘记密码 - 重置密码
     */
    @PostMapping("/reset")
    public Result<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        userService.resetPassword(request);
        return Result.success();
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    public Result<UserResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        if (!jwtHelper.validateRefreshToken(request.getRefreshToken()))
            throw ServiceException.token("exception.token.expired");

        String username = jwtHelper.getUsernameFromToken(request.getRefreshToken());
        UserResponse response = userService.getUserWithToken(username);
        return Result.success(response);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        // 注销访问令牌
        jwtHelper.getTokenFromRequest(httpRequest)
                .filter(jwtHelper::validateAccessToken)
                .ifPresent(jwtHelper::invalidateAccessToken);
        // 注销刷新令牌
        Optional.ofNullable(request)
                .map(RefreshTokenRequest::getRefreshToken)
                .filter(jwtHelper::validateRefreshToken)
                .ifPresent(jwtHelper::invalidateRefreshToken);
        return Result.success();
    }
}
