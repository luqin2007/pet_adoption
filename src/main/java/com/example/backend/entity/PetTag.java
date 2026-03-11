package com.example.backend.entity;

import lombok.Data;

import java.sql.Date;

/**
 * 宠物标签
 */
@Data
public class PetTag implements IId {

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
     * 添加用户 id
     * *外键:user(id) 非空 long*
     */
    Long userId;

    /**
     * 标签内容
     * *非空 varchar(20)*
     */
    String tag;

    /**
     * 创建时间
     * *非空 datetime*
     */
    Date createTime;
}
