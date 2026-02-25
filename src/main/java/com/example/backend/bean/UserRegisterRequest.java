package com.example.backend.bean;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserRegisterRequest {

    @NotBlank(message = "请输入用户名")
    private String username;

    @NotBlank(message = "请输入密码")
    @Length(min = 6)
    private String password;

    @NotBlank(message = "请输入邮箱")
    @Email(message = "邮箱格式错误")
    private String email;

    private String avatar;

    @NotBlank(message = "请输入验证码")
    @Length(min = 6, max = 6, message = "验证码错误")
    private String code;
}
