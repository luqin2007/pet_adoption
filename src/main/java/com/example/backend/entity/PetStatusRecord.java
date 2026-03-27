package com.example.backend.entity;

import com.example.backend.entity.property.PetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

/**
 * 宠物状态变更记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetStatusRecord implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 流浪宠物 id
     * *外键:pet(id) 非空 int*
     */
    private Long petId;

    /**
     * 提交用户 id
     * *外键:user(id) 非空 int*
     */
    private Long userId;

    /**
     * 旧状态
     * *非空 varchar(20)*
     */
    private PetStatus from;

    /**
     * 新状态
     * *非空 varchar(20)*
     */
    private PetStatus to;

    /**
     * 转移说明
     * *text*
     */
    private String description;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

}
