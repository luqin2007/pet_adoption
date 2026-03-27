package com.example.backend.entity;

import lombok.Data;

/**
 * 驱虫药
 */
@Data
public class Dewormer implements IId {

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
     * 类型，内驱/外驱/其他
     * *非空 tinyint*
     */
    private Integer type;

    /**
     * 适用最小年龄
     * *非空 int*
     */
    private Integer minAge;

    /**
     * 总次数
     * *非空 int*
     */
    private Integer times;
}
