package com.example.backend.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.example.backend.entity.property.VolunteerApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 志愿者申请
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerApplication implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 招募计划 id
     * *外键:volunteerRecruitment(id) 非空 bigint*
     */
    private Long recruitmentId;

    /**
     * 申请人 id
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

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
     * *非空 int*
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
     * 过往经历
     * *非空 text*
     */
    private String experience;

    /**
     * 技能
     * *非空 varchar(255)*
     */
    private String skills;

    /**
     * 可服务时间说明
     * *非空 varchar(255)*
     */
    private String timeDesc;

    /**
     * 申请动机
     * *非空 text*
     */
    private String motivation;

    /**
     * 申请状态
     * *非空 varchar(20)*
     */
    private VolunteerApplicationStatus status;

    /**
     * 审核人 id
     * *外键:user(id) bigint*
     */
    private Long reviewerId;

    /**
     * 审核意见
     * *text*
     */
    private String reviewComment;

    /**
     * 审核时间
     * *datetime*
     */
    private Date reviewTime;

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

    public void setStatus(VolunteerApplicationStatus status, String reviewComment, User reviewer) {
        this.status = status;
        if (status.isReviewStatus()) {
            this.reviewComment = reviewComment;
            this.reviewTime = new Date();
            this.reviewerId = reviewer.getId();
        }
    }
}
