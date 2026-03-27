package com.example.backend.dto;

import lombok.Data;

import java.sql.Date;

@Data
public class FirstRegistrationQueryParams {

    // 筛选
    private Long registrar;
    private Long pet;
    private Date date0;
    private Date date1;

    // 筛选 (无索引)
    private String name;
}
