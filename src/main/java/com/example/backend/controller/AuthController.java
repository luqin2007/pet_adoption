package com.example.backend.controller;

import com.example.backend.dto.RefreshTokenRequest;
import com.example.backend.dto.Result;
import com.example.backend.dto.UserResponse;
import com.example.backend.entity.User;
import com.example.backend.service.UserManagerService;
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
 */
@Validated
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;
    private final UserManagerService userManagerService;

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    public Result<UserResponse> refresh(@RequestBody RefreshTokenRequest request) {
        if (!jwtUtils.validateRefreshToken(request.getRefreshToken())) {
            throw new ServiceException(401, "Token 无效或已过期");
        }

        String username = jwtUtils.getUsernameFromToken(request.getRefreshToken());
        User user = userManagerService.getUser(username);
        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);
        UserResponse response = UserResponse.fromEntity(user, accessToken, refreshToken);
        return Result.success(response);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        jwtUtils.getTokenFromRequest(httpRequest)
                .filter(jwtUtils::validateAccessToken)
                .ifPresent(jwtUtils::invalidateAccessToken);
        Optional.ofNullable(request)
                .map(RefreshTokenRequest::getRefreshToken)
                .filter(jwtUtils::validateRefreshToken)
                .ifPresent(jwtUtils::invalidateRefreshToken);
        return Result.success();
    }
}
