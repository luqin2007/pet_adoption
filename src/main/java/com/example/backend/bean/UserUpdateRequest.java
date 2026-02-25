package com.example.backend.bean;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "请输入用户名")
    private String username;

    @NotBlank(message = "请输入密码")
    @Length(min = 6, message = "密码长度不能小于6位")
    private String password;

    @Email(message = "邮箱格式错误")
    private String email;

    private String avatar;

    @Min(value = 0, message = "错误权限")
    @Max(value = 31, message = "错误权限")
    private int role;
}
