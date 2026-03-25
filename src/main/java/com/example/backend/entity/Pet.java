package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

/**
 * 流浪宠物信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pet implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 发现该宠物的用户 id
     * *外键:user(id) 非空 int*
     */
    private Long discoverId;

    /**
     * 宠物名称
     * *varchar(255)*
     */
    private String name;

    /**
     * 宠物年龄（月）
     * *integer*
     */
    private Integer age;

    /**
     * 宠物性别
     * *varchar(10)*
     */
    private String sex;

    /**
     * 宠物类型
     * *varchar(50)*
     */
    private String type;

    /**
     * 宠物品种
     * *varchar(50)*
     */
    private String breed;

    /**
     * 宠物健康情况
     * *varchar(255)*
     */
    private String health;

    /**
     * 宠物描述
     * *text*
     */
    private String description;

    /**
     * 宠物记录状态
     * *非空 tinyint*
     */
    private Integer status;

    /**
     * 是否被废弃
     * *非空 boolean*
     */
    private Boolean isDiscard;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    /**
     * 最后一次修改时间
     * *非空 datetime*
     */
    private Date updateTime;
}
