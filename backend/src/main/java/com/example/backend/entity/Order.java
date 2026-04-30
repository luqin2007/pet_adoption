package com.example.backend.entity;

import com.example.backend.entity.property.OrderType;
import com.example.backend.entity.property.ParentType;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 处方
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("`order`")
public class Order implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 申请人 id
     * *外键:user(id) 非空 bigint*
     */
    private Long allowerId;

    /**
     * 物品 id
     * *外键:item(id) 非空 bigint*
     */
    private Long itemId;

    /**
     * 与之关联的资源 id
     * *非空 bigint*
     */
    private Long parentId;

    /**
     * 关联类型
     * *非空 varchar(20)*
     */
    private ParentType parentType;

    /**
     * 处方类型
     * *非空 varchar(20)*
     */
    private OrderType type;

    /**
     * 数量
     * *非空 decimal(10,5)*
     */
    private BigDecimal count;

    /**
     * 单位
     * *非空 varchar(10)*
     */
    private String unit;

    /**
     * 价格
     * *非空 decimal(10,2)*
     */
    private BigDecimal price;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
