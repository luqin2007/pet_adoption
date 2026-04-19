package com.example.backend.dto;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerApplication;
import com.example.backend.entity.VolunteerRecruitment;
import com.example.backend.entity.property.ParentType;
import com.example.backend.entity.property.VolunteerApplicationStatus;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 志愿者申请响应
 */
@Data
@AllArgsConstructor
public class VolunteerApplicationResponse implements IResponse {

    /**
     * 申请 id
     */
    private Long id;
    /**
     * 招募计划 id
     */
    private Long recruitmentId;
    /**
     * 招募计划标题
     */
    private String recruitmentTitle;
    /**
     * 申请人 id
     */
    private Long userId;
    /**
     * 申请人用户名
     */
    private String username;
    /**
     * 申请人头像
     */
    private String userAvatar;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 区县
     */
    private String district;
    /**
     * 详细地址
     */
    private String address;
    /**
     * 过往经历
     */
    private String experience;
    /**
     * 技能标签
     */
    private String skills;
    /**
     * 可服务时间说明
     */
    private String availableTimeDesc;
    /**
     * 申请动机
     */
    private String motivation;
    /**
     * 申请状态
     */
    private VolunteerApplicationStatus status;
    /**
     * 审核人 id
     */
    private Long reviewerId;
    /**
     * 审核人名称
     */
    private String reviewerName;
    /**
     * 审核人头像
     */
    private String reviewerAvatar;
    /**
     * 审核意见
     */
    private String reviewComment;
    /**
     * 审核时间
     */
    private Date reviewTime;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 根据实体构造响应
     */
    public static VolunteerApplicationResponse create(VolunteerApplication application,
                                                      VolunteerRecruitment recruitment,
                                                      User applicant,
                                                      User reviewer) {
        return new VolunteerApplicationResponse(
                application.getId(),
                application.getRecruitmentId(),
                recruitment == null ? null : recruitment.getTitle(),
                application.getUserId(),
                applicant == null ? null : applicant.getUsername(),
                applicant == null ? null : FileUtils.generateAssetUrl(ParentType.USER, applicant.getId(), applicant.getAvatar()),
                application.getRealName(),
                application.getPhone(),
                application.getAge(),
                application.getProvince(),
                application.getCity(),
                application.getDistrict(),
                application.getAddress(),
                application.getExperience(),
                application.getSkills(),
                application.getTimePlan(),
                application.getMotivation(),
                application.getStatus(),
                application.getReviewerId(),
                reviewer == null ? null : reviewer.getUsername(),
                reviewer == null ? null : FileUtils.generateAssetUrl(ParentType.USER, reviewer.getId(), reviewer.getAvatar()),
                application.getReviewComment(),
                application.getReviewTime(),
                application.getCreateTime(),
                application.getUpdateTime());
    }

    /**
     * 批量构造响应
     */
    public static VolunteerApplicationResponse createBatch(VolunteerApplication application,
                                                           Map<Long, VolunteerRecruitment> recruitments,
                                                           Map<Long, User> users) {
        return create(application,
                recruitments.get(application.getRecruitmentId()),
                users.get(application.getUserId()),
                users.get(application.getReviewerId()));
    }
}
