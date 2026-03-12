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
    private Long id;

    /**
     * 流浪宠物 id
     * *外键:pet(id) 非空 long*
     */
    private Long petId;

    /**
     * 提交用户 id
     * *外键:user(id) 非空 long*
     */
    private Long userId;

    /**
     * 旧状态
     * *非空 tinyint*
     */
    private Integer from;

    /**
     * 新状态
     * *非空 tinyint*
     */
    private Integer to;

    /**
     * 审批状态
     * *非空 tinyint*
     */
    private Integer status;

    /**
     * 转移说明
     * *可空 text*
     */
    private String description;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

}
