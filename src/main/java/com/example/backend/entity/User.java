package com.example.backend.entity;

import lombok.Data;

import java.sql.Date;

/**
 * 用户账户信息
 */
@Data
public class User implements IUserRole {

    /**
     * 用户 id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色
     * 0000 0000 爱心人士
     * 0000 0001 志愿者
     * 0000 0010 救助站工作人员
     * 0000 0100 捐赠者
     * 0000 1000 兽医
     * 0001 0000 超级管理员
     */
    private Integer role;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 最后一次修改时间
     */
    private Date updateTime;
}
