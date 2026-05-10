package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 捐赠物品表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonationItem implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 捐赠 id
     * *外键:donation(id) bigint*
     */
    private Long donationId;

    /**
     * 物品 id
     * *外键:item(id) bigint*
     */
    private Long itemId;

    /**
     * 若库中没有对应物品，手动填写物品名
     * *varchar(255)*
     */
    private String itemName;

    /**
     * 物品类别 id
     * *外键:category(id) 非空 bigint*
     */
    private Long categoryId;

    /**
     * 物品数量
     * *非空 decimal*
     */
    private BigDecimal count;

    /**
     * 若库中没有对应物品，手动填写物品单位
     * *varchar(20)*
     */
    private String unit;

    /**
     * 物品描述
     * *varchar(255)*
     */
    private String description;

    /**
     * 到期时间
     * *非空 datetime*
     */
    private Date expireTime;

    /**
     * 创建时间
     * *datetime*
     */
    private Date createTime;
}
