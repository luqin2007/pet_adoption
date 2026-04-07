package com.example.backend.dto;

import com.example.backend.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;

/**
 * 用户注册 请求体
 */
@Data
public class UserRegisterTable implements ITable {

    @NotBlank(message = "request.user.username")
    private String username;

    @NotBlank(message = "request.user.password")
    @Length(min = 6, message = "request.user.password.short")
    private String password;

    @NotBlank(message = "request.user.email")
    @Email(message = "request.user.email")
    private String email;

    private MultipartFile avatar;

    private String phone;

    @NotBlank(message = "request.user.code")
    @Length(min = 6, max = 6, message = "request.user.code")
    private String code;

    public User createUser(PasswordEncoder passwordEncoder) {
        Date now = new Date(System.currentTimeMillis());
        return new User(null,
                username,
                passwordEncoder.encode(password),
                email,
                0,
                null,
                phone,
                now,
                now);
    }
}
