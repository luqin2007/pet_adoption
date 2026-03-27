package com.example.backend.entity;

import lombok.Data;

/**
 * 疫苗信息
 */
@Data
public class Vaccine implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 物品 id
     * *外键:item(id) 非空 int*
     */
    private Long itemId;

    /**
     * 疾病名称
     * *非空 varchar(255)*
     */
    private String illness;

    /**
     * 接种最小年龄
     * *非空 int*
     */
    private Integer minAge;

    /**
     * 总针数
     * *非空 int*
     */
    private Integer times;
}
