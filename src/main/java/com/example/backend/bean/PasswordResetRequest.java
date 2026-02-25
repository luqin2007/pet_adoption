package com.example.backend.bean;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PasswordResetRequest {

    @NotBlank(message = "请求超时")
    private String id;

    @NotBlank(message = "密码不能为空")
    @Length(min = 6, message = "密码长度不能小于6位")
    private String password;
}
