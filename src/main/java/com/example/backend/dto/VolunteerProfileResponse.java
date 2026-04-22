package com.example.backend.dto;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerProfile;
import com.example.backend.entity.property.ParentType;
import com.example.backend.entity.property.VolunteerProfileStatus;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * 志愿者档案响应
 */
@Data
@AllArgsConstructor
public class VolunteerProfileResponse implements IResponse {

    /**
     * 档案 id
     */
    private Long id;
    /**
     * 用户 id
     */
    private Long userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 用户头像
     */
    private String userAvatar;
    /**
     * 档案状态
     */
    private VolunteerProfileStatus status;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 性别
     */
    private String sex;
    /**
     * 联系电话
     */
    private String phone;
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
     * 技能说明
     */
    private String skills;
    /**
     * 服务意向
     */
    private String serviceIntention;
    /**
     * 可服务时间说明
     */
    private String availableTimeDesc;
    /**
     * 备注
     */
    private String remark;
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
    public static VolunteerProfileResponse create(VolunteerProfile profile, User user) {
        return new VolunteerProfileResponse(
                profile.getId(),
                profile.getUserId(),
                user == null ? null : user.getUsername(),
                user == null ? null : FileUtils.generateAssetUrl(ParentType.USER, user.getId(), user.getAvatar()),
                profile.getStatus(),
                profile.getRealName(),
                profile.getSex(),
                profile.getPhone(),
                profile.getProvince(),
                profile.getCity(),
                profile.getDistrict(),
                profile.getAddress(),
                profile.getSkills(),
                profile.getServiceDesc(),
                profile.getTimeDesc(),
                profile.getRemark(),
                profile.getCreateTime(),
                profile.getUpdateTime());
    }

    /**
     * 批量构造响应
     */
    public static VolunteerProfileResponse createBatch(VolunteerProfile profile, Map<Long, User> users) {
        return create(profile, users.get(profile.getUserId()));
    }
}
