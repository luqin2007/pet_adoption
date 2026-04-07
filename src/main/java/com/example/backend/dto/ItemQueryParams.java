package com.example.backend.dto;

import com.example.backend.entity.Item;
import lombok.Data;

import java.util.Set;

@Data
public class ItemQueryParams implements IParam<Item> {

    private Set<Long> category;

    private String keyword;
}
