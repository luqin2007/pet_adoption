package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 康复记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RehabRecord implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 康复计划 id
     * *外键:rehabPlan(id) 非空 bigint*
     */
    private Long planId;

    /**
     * 执行人员 id
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 执行步骤
     * *非空 varchar(255)*
     */
    private String step;

    /**
     * 宠物反应
     * *非空 varchar(255)*
     */
    private String reaction;

    /**
     * 备注
     * *非空 varchar(255)*
     */
    private String note;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
