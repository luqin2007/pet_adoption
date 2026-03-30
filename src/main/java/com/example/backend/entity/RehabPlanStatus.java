package com.example.backend.entity;

import com.example.backend.entity.property.RehabPlanStatusProp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 康复计划状态变更记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RehabPlanStatus implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 提交用户 id
     * *外键:user(id) 非空 int*
     */
    private Long userId;

    /**
     * 康复计划 id
     * *外键:plan(id) 非空 int*
     */
    private Long planId;

    /**
     * 新状态
     * *非空 varchar(20)*
     */
    private RehabPlanStatusProp status;

    /**
     * 变更原因
     * *非空 varchar(255)*
     */
    private String reason;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
