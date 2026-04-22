package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.property.VolunteerRecruitmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 志愿者招募计划
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerRecruitment implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 招募标题
     * *非空 varchar(255)*
     */
    private String title;

    /**
     * 招募说明
     * *text*
     */
    private String description;

    /**
     * 招募要求
     * *text*
     */
    private String requirement;

    /**
     * 服务地点
     * *非数据库字段，存储于 location 表*
     */
    @TableField(exist = false)
    private String serviceAddress;

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
     * 招募人数
     * *非空 int*
     */
    private Integer headcount;

    /**
     * 已申请人数
     * *非空 int*
     */
    private Integer appliedCount;

    /**
     * 招募开始时间
     * *非空 datetime*
     */
    private Date startTime;

    /**
     * 招募结束时间
     * *非空 datetime*
     */
    private Date endTime;

    /**
     * 招募状态
     * *非空 varchar(20)*
     */
    private VolunteerRecruitmentStatus status;

    /**
     * 发布人 id
     * *外键:user(id) 非空 bigint*
     */
    private Long publisherId;

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
