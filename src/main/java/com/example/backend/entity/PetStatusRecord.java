package com.example.backend.entity;

import lombok.Data;

import java.sql.Date;

@Data
public class PetStatusRecord {

    Long id;
    Long petId;
    Long userId;
    Integer from;
    Integer to;
    Date time;
    String description;
}
