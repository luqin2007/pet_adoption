package com.example.backend.dto;

import com.example.backend.entity.User;
import com.example.backend.util.AuthUtils;
import com.example.backend.util.C;
import com.example.backend.util.FileUtils;
import lombok.Data;

/**
 * 用户响应，返回用户信息
 */
@Data
public class UserResponse {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 身份
     */
    private Integer role;

    /**
     * 访问令牌，仅登录/注册/刷新令牌时返回
     */
    private String accessToken;

    /**
     * 刷新令牌，仅登录/注册/刷新令牌时返回
     */
    private String refreshToken;

    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setAvatar(FileUtils.generateAssetUrl(C.PARENT_USER_AVATAR, user.getId(), user.getAvatar()));
        response.setRole(AuthUtils.getRoleCode(user.getRole()));
        return response;
    }
}
