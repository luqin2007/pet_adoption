package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

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
}
