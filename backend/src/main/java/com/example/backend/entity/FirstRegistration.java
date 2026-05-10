package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 初诊登记
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FirstRegistration implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 登记人 id
     * *外键:user(id) long*
     */
    private Long registrarId;

    /**
     * 宠物 id
     * *外键:pet(id) long*
     */
    private Long petId;

    /**
     * 宠物名称
     * *varchar(20)*
     */
    private String name;

    /**
     * 宠物年龄（月）
     * *非空 integer*
     */
    private Integer age;

    /**
     * 宠物体重 kg
     * *非空 decimal(6,2)*
     */
    private Double weight;

    /**
     * 宠物体温 ℃
     * *非空 decimal(3,2)*
     */
    private Double temperature;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
