package com.example.backend.dto;

import com.example.backend.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;

/**
 * 用户注册 请求体
 */
@Data
public class UserRegisterRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "请输入用户名")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "请输入密码")
    @Length(min = 6)
    private String password;

    /**
     * 邮箱
     */
    @NotBlank(message = "请输入邮箱")
    @Email(message = "邮箱格式错误")
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 验证码
     */
    @NotBlank(message = "请输入验证码")
    @Length(min = 6, max = 6, message = "验证码错误")
    private String code;

    public User createUser(PasswordEncoder passwordEncoder) {
        Date now = new Date(System.currentTimeMillis());
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setRole(0);
        user.setAvatar(avatar);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        return user;
    }
}
