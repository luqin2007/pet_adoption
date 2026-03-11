package com.example.backend.entity;

import lombok.Data;

import java.sql.Date;

/**
 * 宠物状态变更记录
 */
@Data
public class PetStatusRecord implements IId {

    /**
     * *主键 long*
     */
    Long id;

    /**
     * 流浪宠物 id
     * *外键:pet(id) 非空 long*
     */
    Long petId;

    /**
     * 提交用户 id
     * *外键:user(id) 非空 long*
     */
    Long userId;

    /**
     * 旧状态
     * *非空 tinyint*
     */
    Integer from;

    /**
     * 新状态
     * *非空 tinyint*
     */
    Integer to;

    /**
     * 转移说明
     * *可空 text*
     */
    String description;

    /**
     * 创建时间
     * *非空 datetime*
     */
    Date createTime;

}
