package com.example.backend.entity;

import lombok.Data;

import java.sql.Date;

/**
 * 用户账户信息
 */
@Data
public class User implements IUserRole, IId {

    /**
     * *主键 long*
     */
    private Long id;

    /**
     * 用户名
     * *非空 唯一 varchar(255)*
     */
    private String username;

    /**
     * 密码
     * *非空 varchar(255)*
     */
    private String password;

    /**
     * 邮箱
     * *非空 唯一 varchar(255)*
     */
    private String email;

    /**
     * 角色
     * *非空 tinyint*
     */
    private Integer role;

    /**
     * 头像
     * *可空 varchar(255)*
     */
    private String avatar;

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
