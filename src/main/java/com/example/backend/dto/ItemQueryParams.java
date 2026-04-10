package com.example.backend.dto;

import lombok.Data;

import java.util.Set;

@Data
public class ItemQueryParams implements IParam {

    private Set<Long> category;

    private String keyword;
}
