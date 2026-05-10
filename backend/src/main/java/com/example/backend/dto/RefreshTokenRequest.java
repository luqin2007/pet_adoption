package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest implements IRequest {

    @NotBlank(message = "request.token.refresh")
    private String refreshToken;
}
