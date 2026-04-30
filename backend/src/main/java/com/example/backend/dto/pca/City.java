package com.example.backend.dto.pca;

import lombok.Data;

import java.util.List;

@Data
public class City {

    private String code;

    private String name;

    private List<Area> children;
}
