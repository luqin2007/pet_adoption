package com.example.backend.bean;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String username;
    private String password;
    private String email;
    private int role;
    private String avatar;
}
