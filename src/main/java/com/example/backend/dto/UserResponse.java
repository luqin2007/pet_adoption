package com.example.backend.dto;

import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.entity.property.UserRole;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户响应，返回用户信息
 */
@Data
@AllArgsConstructor
public class UserResponse implements IResponse {

    private Long id;
    private String username;
    private String email;
    private String avatar;
    private Integer role;
    private String phone;
    /**
     * 访问令牌，仅登录/注册/刷新令牌时返回
     */
    private String accessToken;
    /**
     * 刷新令牌，仅登录/注册/刷新令牌时返回
     */
    private String refreshToken;

    public static UserResponse create(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                FileUtils.generateAssetUrl(ParentType.USER, user.getId(), user.getAvatar()),
                UserRole.rezip(user.getRole()),
                user.getPhone(),
                null,
                null
        );
    }
}
