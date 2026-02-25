package com.example.backend.bean;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "RefreshToken 错误")
    private String refreshToken;
}
