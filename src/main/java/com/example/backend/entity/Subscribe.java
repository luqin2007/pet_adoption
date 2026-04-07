package com.example.backend.entity;

import com.example.backend.entity.property.SubscribeAction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存订阅
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscribe implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 订阅 id（物品、类型、库存等）
     * *非空 bigint*
     */
    private Long elementId;

    /**
     * 订阅用户
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 触发类型
     * *非空 varchar(20)*
     */
    private SubscribeAction action;

    /**
     * 触发阈值
     * *decimal*
     */
    private BigDecimal count;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
