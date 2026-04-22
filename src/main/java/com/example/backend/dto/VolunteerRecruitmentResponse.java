package com.example.backend.dto;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerRecruitment;
import com.example.backend.entity.property.ParentType;
import com.example.backend.entity.property.VolunteerRecruitmentStatus;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 志愿者招募计划响应
 */
@Data
@AllArgsConstructor
public class VolunteerRecruitmentResponse implements IResponse {

    /**
     * 招募计划 id
     */
    private Long id;
    /**
     * 招募标题
     */
    private String title;
    /**
     * 招募说明
     */
    private String description;
    /**
     * 招募要求
     */
    private String requirement;
    /**
     * 服务地点
     */
    private String serviceAddress;
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
     * 招募人数
     */
    private Integer headcount;
    /**
     * 已申请人数
     */
    private Integer appliedCount;
    /**
     * 招募开始时间
     */
    private Date startTime;
    /**
     * 招募结束时间
     */
    private Date endTime;
    /**
     * 招募状态
     */
    private VolunteerRecruitmentStatus status;
    /**
     * 发布人 id
     */
    private Long publisherId;
    /**
     * 发布人名称
     */
    private String publisherName;
    /**
     * 发布人头像
     */
    private String publisherAvatar;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 根据实体构造响应
     */
    public static VolunteerRecruitmentResponse create(VolunteerRecruitment recruitment, User publisher) {
        return new VolunteerRecruitmentResponse(
                recruitment.getId(),
                recruitment.getTitle(),
                recruitment.getDescription(),
                recruitment.getRequirement(),
                recruitment.getServiceAddress(),
                recruitment.getProvince(),
                recruitment.getCity(),
                recruitment.getDistrict(),
                recruitment.getHeadcount(),
                recruitment.getAppliedCount(),
                recruitment.getStartTime(),
                recruitment.getEndTime(),
                recruitment.getStatus(),
                recruitment.getPublisherId(),
                publisher == null ? null : publisher.getUsername(),
                publisher == null ? null : FileUtils.generateAssetUrl(ParentType.USER, publisher.getId(), publisher.getAvatar()),
                recruitment.getCreateTime(),
                recruitment.getUpdateTime());
    }

    /**
     * 批量构造响应
     */
    public static VolunteerRecruitmentResponse createBatch(VolunteerRecruitment recruitment, Map<Long, User> users) {
        return create(recruitment, users.get(recruitment.getPublisherId()));
    }
}
