package com.example.backend.entity;

import com.example.backend.entity.property.LostPetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 走失宠物报备记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LostPet implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 报备人 id
     * *外键:user(id) 非空 bigint*
     */
    private Long ownerId;

    /**
     * 宠物名称
     * *非空 varchar(255)*
     */
    private String name;

    /**
     * 宠物年龄（月）
     * *integer*
     */
    private Integer age;

    /**
     * 宠物性别
     * *非空 varchar(10)*
     */
    private String sex;

    /**
     * 宠物类型
     * *非空 varchar(50)*
     */
    private String type;

    /**
     * 宠物品种
     * *非空 varchar(50)*
     */
    private String breed;

    /**
     * 宠物特征描述（毛色、体型、特殊标记等）
     * *text*
     */
    private String features;

    /**
     * 走失时间
     * *非空 datetime*
     */
    private Date lostTime;

    /**
     * 联系方式
     * *非空 varchar(50)*
     */
    private String phone;

    /**
     * 补充描述
     * *text*
     */
    private String description;

    /**
     * 已找到的流浪宠物 id
     * *外键:pet(id) bigint*
     */
    private Long petId;

    /**
     * 状态
     * *非空 varchar(20)*
     */
    private LostPetStatus status;

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
