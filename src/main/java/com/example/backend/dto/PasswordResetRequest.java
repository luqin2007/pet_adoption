package com.example.backend.dto;

import com.example.backend.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;

/**
 * 重置密码请求体
 */
@Data
public class PasswordResetRequest {

    /**
     * 请求 ID
     */
    @NotBlank(message = "无效请求")
    private String id;

    /**
     * 新密码
     */
    @NotBlank(message = "密码不能为空")
    @Length(min = 6, message = "密码长度不能小于6位")
    private String password;

    public void applyTo(User user, PasswordEncoder passwordEncoder) {
        user.setPassword(passwordEncoder.encode(password));
        user.setUpdateTime(new Date(System.currentTimeMillis()));
    }
}
