package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 走失宠物误匹配记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LostPetMismatch implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 走失宠物 id
     * *外键:lostPet(id) 非空 bigint*
     */
    private Long lostPetId;

    /**
     * 被排除的流浪宠物 id
     * *外键:pet(id) 非空 bigint*
     */
    private Long petId;

    /**
     * 操作用户 id
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
