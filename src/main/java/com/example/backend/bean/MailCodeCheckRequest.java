package com.example.backend.bean;

import lombok.Data;

@Data
public class MailCodeCheckRequest {

    private String email;
    private String code;
}
