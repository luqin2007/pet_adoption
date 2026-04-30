package com.example.backend.entity;

import com.example.backend.entity.property.DeliveryType;
import com.example.backend.entity.property.DonationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 捐赠信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Donation implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 捐赠人
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 交付方式
     * *非空 varchar(20)*
     */
    private DeliveryType delivery;

    /**
     * 交付方式为 ADDRESS 时记录取货地址
     * *varchar(255)*
     */
    private String address;

    /**
     * 交付方式为 EXPRESS 时记录快递单号
     * *varchar(50)*
     */
    private String trackingNumber;

    /**
     * 留言
     * *varchar(255)*
     */
    private String description;

    /**
     * 捐赠状态
     * *非空 varchar(20)*
     */
    private DonationStatus status;

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
