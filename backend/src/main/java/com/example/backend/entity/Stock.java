package com.example.backend.entity;

import com.example.backend.entity.property.SourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 库存物品
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 对应物品
     * *外键:item(id) 非空 bigint*
     */
    private Long itemId;

    /**
     * 捐赠人/采购人 id
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 数量
     * *非空 decimal*
     */
    private BigDecimal count;

    /**
     * 来源类型
     * *非空 varchar(20)*
     */
    private SourceType sourceType;

    /**
     * 有效期
     * *非空 datetime*
     */
    private Date expireTime;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    /**
     * 修改时间
     * *非空 datetime*
     */
    private Date updateTime;
}
