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
public class UserRegisterTable {

    @NotBlank(message = "请输入用户名")
    private String username;

    @NotBlank(message = "请输入密码")
    @Length(min = 6)
    private String password;

    @NotBlank(message = "请输入邮箱")
    @Email(message = "邮箱格式错误")
    private String email;

    private MultipartFile avatar;

    private String phone;

    @NotBlank(message = "请输入验证码")
    @Length(min = 6, max = 6, message = "验证码错误")
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
