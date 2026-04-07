package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

/**
 * 宠物标签
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetTag implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 流浪宠物 id
     * *外键:pet(id) 非空 bigint*
     */
    private Long petId;

    /**
     * 添加用户 id
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 标签内容
     * *非空 varchar(20)*
     */
    private String tag;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
