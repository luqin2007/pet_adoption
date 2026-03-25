package com.example.backend.entity;

import lombok.Data;

@Data
public class Item implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 物品名
     * *非空 varchar(255)*
     */
    private String name;
}
