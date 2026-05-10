package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户账户信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements IUserRole, IId {

    /**
     * *主键 bigint*
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
     * *varchar(255)*
     */
    private String avatar;

    /**
     * 联系方式
     * *varchar(50)*
     */
    private String phone;

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
