package com.example.backend.dto;

import com.example.backend.entity.User;
import com.example.backend.util.AuthUtils;
import com.example.backend.util.StringUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;

import static com.example.backend.util.C.ROLE_MASK_MAX;
import static com.example.backend.util.C.ROLE_MASK_MIN;

/**
 * 用户更新 请求体
 */
@Data
public class UserUpdateRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "请输入用户名")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "请输入密码")
    @Length(min = 6, message = "密码长度不能小于6位")
    private String password;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式错误")
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色
     */
    @Range(min = ROLE_MASK_MIN, max = ROLE_MASK_MAX, message = "错误权限")
    private int role;

    public void apply(User user, PasswordEncoder passwordEncoder) {
        user.setUsername(username);
        if (StringUtils.hasText(password))
            user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(AuthUtils.getRoleCode(role));
        user.setAvatar(avatar);
        user.setUpdateTime(new Date(System.currentTimeMillis()));
    }
}
