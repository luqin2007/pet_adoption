package com.example.backend.entity;

import lombok.Data;

@Data
public class PetInformation {

    private Long id;
    private PetImage photo;
    private String name;
    private Integer minAge;
    private Integer maxAge;
    private String sex;
    private String type;
    private String breed;
    private String health;
    private String vaccine;
    private int status;
    private String description;
    private String article;
}
