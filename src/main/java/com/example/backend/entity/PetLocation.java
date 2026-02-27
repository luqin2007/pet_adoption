package com.example.backend.entity;

import lombok.Data;

import java.sql.Date;

/**
 * 发现流浪宠物的位置
 */
@Data
public class PetLocation {

    private Long id;

    /**
     * 宠物 id
     */
    private Long petId;

    /**
     * 发现者 id
     */
    private Long userId;

    /**
     * 发现省份
     */
    private String province;

    /**
     * 发现城市
     */
    private String city;

    /**
     * 发现县/县级市
     */
    private String county;

    /**
     * 详细地址
     */
    private String detailAddress;

    /**
     * 发现时间
     */
    private Date date;
}
