package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 处方
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 申请人 id
     * *外键:user(id) 非空 int*
     */
    private Long allowerId;

    /**
     * 物品 id
     * *外键:item(id) 非空 int*
     */
    private Long itemId;

    /**
     * 与之关联的资源 id
     * *非空 long*
     */
    private Long parentId;

    /**
     * 关联类型
     * *非空 varchar(20)*
     */
    private String parentType;

    /**
     * 申请类型
     * *非空 int*
     */
    private Integer type;

    /**
     * 数量
     * *非空 decimal(10,5)*
     */
    private Double count;

    /**
     * 单位
     * *非空 varchar(10)*
     */
    private String unit;

    /**
     * 价格
     * *非空 decimal(10,2)*
     */
    private Double price;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
