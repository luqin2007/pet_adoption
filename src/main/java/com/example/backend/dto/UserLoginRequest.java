package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserLoginRequest implements IRequest {

    @NotBlank(message = "request.user.username")
    private String username;

    @NotBlank(message = "request.user.password")
    @Length(min = 6, message = "request.user.password.short")
    private String password;
}
