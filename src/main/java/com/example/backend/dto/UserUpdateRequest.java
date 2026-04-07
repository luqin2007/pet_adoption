package com.example.backend.dto;

import com.example.backend.entity.User;
import com.example.backend.entity.property.UserRole;
import com.example.backend.util.StringUtils;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;

/**
 * 用户更新 请求体
 */
@Data
public class UserUpdateRequest implements IRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "request.user.username")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "request.user.password")
    @Length(min = 6, message = "request.user.password.short")
    private String password;

    /**
     * 邮箱
     */
    @Email(message = "request.user.email")
    private String email;

    /**
     * 角色
     */
    @Range(min = 0, max = UserRole.MAX_ROLE, message = "request.user.role")
    private int role;

    public void applyTo(User user, PasswordEncoder passwordEncoder) {
        user.setUsername(username);
        if (StringUtils.hasText(password))
            user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(UserRole.rezip(role));
        user.setUpdateTime(new Date(System.currentTimeMillis()));
    }
}
