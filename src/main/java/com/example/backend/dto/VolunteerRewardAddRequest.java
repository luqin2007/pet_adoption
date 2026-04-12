package com.example.backend.dto;

import com.example.backend.entity.VolunteerReward;
import com.example.backend.entity.property.VolunteerRewardStatus;
import com.example.backend.entity.property.VolunteerRewardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 志愿者激励创建请求
 */
@Data
public class VolunteerRewardAddRequest implements IRequest, IValidatedRequest {

    /**
     * 志愿者 id
     */
    @NotNull(message = "志愿者不能为空")
    private Long volunteerId;
    /**
     * 统计开始时间
     */
    @NotNull(message = "统计开始时间不能为空")
    private Date periodStart;
    /**
     * 统计结束时间
     */
    @NotNull(message = "统计结束时间不能为空")
    private Date periodEnd;
    private Integer serviceCount;
    private BigDecimal totalHours;
    @NotBlank(message = "激励类型不能为空")
    private String rewardType;
    private String rewardValue;
    @NotBlank(message = "激励原因不能为空")
    private String rewardReason;
    private String remark;

    /**
     * 创建激励实体
     */
    public VolunteerReward create(Long issuerId) {
        Date now = new Date();
        return new VolunteerReward(
                null,
                volunteerId,
                periodStart,
                periodEnd,
                serviceCount,
                totalHours,
                VolunteerRewardType.get(rewardType),
                rewardValue,
                rewardReason,
                VolunteerRewardStatus.PENDING,
                issuerId,
                null,
                remark,
                now,
                now);
    }

    /**
     * 校验激励类型和时间范围
     */
    @Override
    public void validate(Errors errors) {
        validateEnum(errors, VolunteerRewardAddRequest::getRewardType, VolunteerRewardType.class, "request.volunteer.reward.type");
        validateTime(errors, VolunteerRewardAddRequest::getPeriodStart, VolunteerRewardAddRequest::getPeriodEnd);
    }
}
