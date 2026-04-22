package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.property.VolunteerProfileStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 志愿者档案
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerProfile implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 用户 id
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 档案状态
     * *非空 varchar(20)*
     */
    private VolunteerProfileStatus status;

    /**
     * 真实姓名
     * *非空 varchar(100)*
     */
    private String realName;

    /**
     * 性别
     * *非空 varchar(20)*
     */
    private String sex;

    /**
     * 联系电话
     * *非空 varchar(50)*
     */
    private String phone;

    /**
     * 年龄
     * *非空 integer*
     */
    private Integer age;

    /**
     * 省份
     * *非数据库字段，存储于 location 表*
     */
    @TableField(exist = false)
    private String province;

    /**
     * 城市
     * *非数据库字段，存储于 location 表*
     */
    @TableField(exist = false)
    private String city;

    /**
     * 区县
     * *非数据库字段，存储于 location 表*
     */
    @TableField(exist = false)
    private String district;

    /**
     * 详细地址
     * *非数据库字段，存储于 location 表*
     */
    @TableField(exist = false)
    private String address;

    /**
     * 技能说明
     * *text*
     */
    private String skills;

    /**
     * 服务意向
     * *text*
     */
    private String serviceDesc;

    /**
     * 可服务时间说明
     * *varchar(255)*
     */
    private String timeDesc;

    /**
     * 备注
     * *text*
     */
    private String remark;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    /**
     * 修改时间
     * *非空 datetime*
     */
    private Date updateTime;
}
