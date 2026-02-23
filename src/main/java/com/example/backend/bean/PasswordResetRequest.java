package com.example.backend.bean;

import lombok.Data;

@Data
public class PasswordResetRequest {

    private String id;
    private String password;
}
