package com.example.backend.entity;

import lombok.Data;

import java.sql.Date;

@Data
public class User {

    private Long id;
    private String username;
    private String password;
    private String email;
    private int role;
    private String avatar;
    private Date createTime;
    private Date updateTime;
}
