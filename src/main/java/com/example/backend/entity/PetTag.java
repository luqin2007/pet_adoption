package com.example.backend.entity;

import lombok.Data;

import java.sql.Date;

@Data
public class PetTag {

    Long id;
    Long petId;
    Long userId;
    String tag;
    Date createTime;
}
