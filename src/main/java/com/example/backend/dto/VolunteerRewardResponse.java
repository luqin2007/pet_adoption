package com.example.backend.dto;

import com.example.backend.entity.User;
import com.example.backend.entity.VolunteerReward;
import com.example.backend.entity.property.ParentType;
import com.example.backend.entity.property.VolunteerRewardStatus;
import com.example.backend.entity.property.VolunteerRewardType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 志愿者激励响应
 */
@Data
@AllArgsConstructor
public class VolunteerRewardResponse implements IResponse {

    /**
     * 激励记录 id
     */
    private Long id;
    /**
     * 志愿者 id
     */
    private Long volunteerId;
    /**
     * 志愿者名称
     */
    private String volunteerName;
    /**
     * 志愿者头像
     */
    private String volunteerAvatar;
    /**
     * 统计开始时间
     */
    private Date periodStart;
    /**
     * 统计结束时间
     */
    private Date periodEnd;
    /**
     * 服务次数
     */
    private Integer serviceCount;
    /**
     * 累计服务时长
     */
    private BigDecimal totalHours;
    /**
     * 激励类型
     */
    private VolunteerRewardType rewardType;
    /**
     * 激励内容
     */
    private String rewardValue;
    /**
     * 激励原因
     */
    private String rewardReason;
    /**
     * 激励状态
     */
    private VolunteerRewardStatus status;
    /**
     * 发放人 id
     */
    private Long issuerId;
    /**
     * 发放人名称
     */
    private String issuerName;
    /**
     * 发放人头像
     */
    private String issuerAvatar;
    /**
     * 发放时间
     */
    private Date issueTime;
    /**
     * 备注
     */
    private String remark;
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
    public static VolunteerRewardResponse create(VolunteerReward reward, User volunteer, User issuer) {
        return new VolunteerRewardResponse(
                reward.getId(),
                reward.getVolunteerId(),
                volunteer == null ? null : volunteer.getUsername(),
                volunteer == null ? null : FileUtils.generateAssetUrl(ParentType.USER, volunteer.getId(), volunteer.getAvatar()),
                reward.getPeriodStart(),
                reward.getPeriodEnd(),
                reward.getServiceCount(),
                reward.getTotalHours(),
                reward.getRewardType(),
                reward.getRewardValue(),
                reward.getRewardReason(),
                reward.getStatus(),
                reward.getIssuerId(),
                issuer == null ? null : issuer.getUsername(),
                issuer == null ? null : FileUtils.generateAssetUrl(ParentType.USER, issuer.getId(), issuer.getAvatar()),
                reward.getIssueTime(),
                reward.getRemark(),
                reward.getCreateTime(),
                reward.getUpdateTime());
    }

    /**
     * 批量构造响应
     */
    public static VolunteerRewardResponse createBatch(VolunteerReward reward, Map<Long, User> users) {
        return create(reward, users.get(reward.getVolunteerId()), users.get(reward.getIssuerId()));
    }
}
