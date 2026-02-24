package com.example.backend.controller;

import com.example.backend.bean.RefreshTokenRequest;
import com.example.backend.bean.Result;
import com.example.backend.bean.UserLoginResponse;
import com.example.backend.entity.User;
import com.example.backend.service.UserManagerService;
import com.example.backend.util.JwtUtils;
import com.example.backend.util.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtils jwtUtils;
    private final UserManagerService userManagerService;

    @PostMapping("/refresh")
    public Result<UserLoginResponse> refresh(@RequestBody RefreshTokenRequest request) {
        if (!jwtUtils.validateRefreshToken(request.getRefreshToken())) {
            throw new ServiceException(401, "Token 无效或已过期");
        }

        String username = jwtUtils.getUsernameFromToken(request.getRefreshToken());
        User user = userManagerService.getUser(username);
        String accessToken = jwtUtils.generateAccessToken(user);
        String refreshToken = jwtUtils.generateRefreshToken(user);
        UserLoginResponse response = UserLoginResponse.fromEntity(user, accessToken, refreshToken);
        return Result.success(response);
    }

    @GetMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
