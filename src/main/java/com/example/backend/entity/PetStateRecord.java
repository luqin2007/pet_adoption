package com.example.backend.entity;

import lombok.Data;

import java.sql.Date;

@Data
public class PetStateRecord {

    Long id;
    Long userId;
    Long petId;
    int from;
    int to;
    Date time;
    String description;
}
