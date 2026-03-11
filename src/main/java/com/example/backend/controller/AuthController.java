package com.example.backend.controller;

import com.example.backend.dto.RefreshTokenRequest;
import com.example.backend.dto.Result;
import com.example.backend.dto.UserResponse;
import com.example.backend.service.UserService;
import com.example.backend.util.JwtUtils;
import com.example.backend.util.ServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * 认证相关 API
 * - 刷新认证
 * - 登出
 * TODO 是否需要创建对应 Service 层？
 */
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;
    private final UserService userService;

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    public Result<UserResponse> refresh(@RequestBody RefreshTokenRequest request) {
        if (!jwtUtils.validateRefreshToken(request.getRefreshToken()))
            throw ServiceException.token("Token 无效或已过期");

        String username = jwtUtils.getUsernameFromToken(request.getRefreshToken());
        UserResponse response = userService.getUser(username);
        return Result.success(response);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        // 注销访问令牌
        jwtUtils.getTokenFromRequest(httpRequest)
                .filter(jwtUtils::validateAccessToken)
                .ifPresent(jwtUtils::invalidateAccessToken);
        // 注销刷新令牌
        Optional.ofNullable(request)
                .map(RefreshTokenRequest::getRefreshToken)
                .filter(jwtUtils::validateRefreshToken)
                .ifPresent(jwtUtils::invalidateRefreshToken);
        return Result.success();
    }
}
