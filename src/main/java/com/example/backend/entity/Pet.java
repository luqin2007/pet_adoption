package com.example.backend.entity;

import lombok.Data;

import java.sql.Date;

/**
 * 流浪宠物信息
 */
@Data
public class Pet implements IId {

    /**
     * *主键 long*
     */
    private Long id;

    /**
     * 发现该宠物的用户 id
     * *外键:user(id) 非空 long*
     */
    private Long userId;

    /**
     * 宠物名称
     * *可空 varchar(255)*
     */
    private String name;

    /**
     * 宠物最小年龄（月）
     * *可空 integer*
     */
    private Integer minAge;

    /**
     * 宠物最大年龄（月）
     * *可空 integer*
     */
    private Integer maxAge;

    /**
     * 宠物性别
     * *可空 varchar(10)*
     */
    private String sex;

    /**
     * 宠物类型
     * *可空 varchar(50)*
     */
    private String type;

    /**
     * 宠物品种
     * *可空 varchar(50)*
     */
    private String breed;

    /**
     * 宠物健康情况
     * *可空 varchar(255)*
     */
    private String health;

    /**
     * 宠物疫苗情况
     * *可空 varchar(255)*
     */
    private String vaccine;

    /**
     * 宠物描述
     * *可空 text*
     */
    private String description;

    /**
     * 宠物状态
     * *非空 tinyint*
     */
    private Integer status;

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
