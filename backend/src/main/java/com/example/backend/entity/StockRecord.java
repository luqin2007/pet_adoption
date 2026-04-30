package com.example.backend.entity;

import com.example.backend.entity.property.StockAction;
import com.example.backend.entity.property.SourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存流转记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRecord implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 关联库存
     * *外键:stock(id) 非空 bigint*
     */
    private Long stockId;

    /**
     * 操作用户
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 动作
     * *非空 varchar(20)*
     */
    private StockAction action;

    /**
     * 来源类型
     * *非空 varchar(20)*
     */
    private SourceType sourceType;

    /**
     * 变动数量
     * *非空 decimal*
     */
    private BigDecimal count;

    /**
     * 剩余数量
     * *非空 decimal*
     */
    private BigDecimal remain;

    /**
     * 单价
     * *非空 decimal*
     */
    private BigDecimal price;

    /**
     * 总价
     * *非空 decimal*
     */
    private BigDecimal totalPrice;

    /**
     * 用途 / 销毁原因
     * *varchar(255)*
     */
    private String purpose;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
