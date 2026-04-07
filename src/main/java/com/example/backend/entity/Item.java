package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 分类 id
     * *外键:category(id) 非空 bigint*
     */
    private Long categoryId;

    /**
     * 物品名
     * *非空 varchar(255)*
     */
    private String name;

    /**
     * 物品描述
     * *非空 text*
     */
    private String description;

    /**
     * 单位
     * *非空 varchar(20)*
     */
    private String unit;

    /**
     * 已废弃
     * *非空 boolean*
     */
    private Boolean isDiscard;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    /**
     * 更新时间
     * *非空 datetime*
     */
    private Date updateTime;
}
