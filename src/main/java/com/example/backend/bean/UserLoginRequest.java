package com.example.backend.bean;

import lombok.Data;

@Data
public class UserLoginRequest {

    private String username;
    private String password;
}
