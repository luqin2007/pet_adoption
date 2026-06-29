package com.example.backend.dto;

import lombok.Data;

@Data
public class ConfigPropertyRequest {

    private String key;

    private String value;
}
