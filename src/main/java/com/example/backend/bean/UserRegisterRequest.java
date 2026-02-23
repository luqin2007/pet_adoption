package com.example.backend.bean;

import lombok.Data;

@Data
public class UserRegisterRequest {

    private String username;
    private String password;
    private String email;
    private String avatar;
    private String code;
}
