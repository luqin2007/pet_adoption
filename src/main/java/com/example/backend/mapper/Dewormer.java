package com.example.backend.mapper;

import com.example.backend.entity.IId;
import lombok.Data;

/**
 * 驱虫药
 */
@Data
public class Dewormer implements IId {

    /**
     * *主键 int*
     */
    Long id;

    /**
     * 物品 id
     * *外键:item(id) 非空 int*
     */
    Long itemId;

    /**
     * 类型，内驱/外驱/其他
     * *非空 tinyint*
     */
    Integer type;

    /**
     * 适用最小年龄
     * *非空 int*
     */
    Integer minAge;

    /**
     * 总次数
     * *非空 int*
     */
    Integer times;
}
