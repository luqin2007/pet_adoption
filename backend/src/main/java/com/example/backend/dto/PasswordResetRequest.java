package com.example.backend.dto;

import com.example.backend.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

/**
 * 重置密码请求体
 */
@Data
public class PasswordResetRequest implements IRequest {

    /**
     * 请求 ID
     */
    @NotBlank(message = "request.user.reset.id")
    private String id;

    /**
     * 新密码
     */
    @NotBlank(message = "request.user.password")
    @Length(min = 6, message = "request.user.password.short")
    private String password;

    public void applyTo(User user, PasswordEncoder passwordEncoder) {
        user.setPassword(passwordEncoder.encode(password));
        user.setUpdateTime(new Date());
    }
}
