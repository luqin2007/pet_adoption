package com.example.backend.dto;

import com.example.backend.entity.User;
import com.example.backend.util.Bits;
import com.example.backend.util.C;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.example.backend.util.C.USER_ROLE_REZIP_MAP;

/**
 * 用户响应，返回用户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
     * 联系方式
     */
    private String phone;

    /**
     * 访问令牌，仅登录/注册/刷新令牌时返回
     */
    private String accessToken;

    /**
     * 刷新令牌，仅登录/注册/刷新令牌时返回
     */
    private String refreshToken;

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                FileUtils.generateAssetUrl(C.PARENT_USER, user.getId(), user.getAvatar()),
                Bits.rezip(USER_ROLE_REZIP_MAP, user.getRole()),
                user.getPhone(),
                null,
                null
        );
    }
}
