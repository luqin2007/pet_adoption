package com.example.backend.mapper;

import com.example.backend.dto.VolunteerRewardQueryParams;
import com.example.backend.entity.VolunteerReward;
import com.example.backend.entity.property.VolunteerRewardStatus;
import com.example.backend.entity.property.VolunteerRewardType;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (volunteerId, periodStart, periodEnd)
 * - (status, issueTime)
 */
@Mapper
public interface VolunteerRewardMapper extends IBaseMapper<VolunteerReward> {

    /**
     * 根据查询条件筛选激励记录
     */
    default MPLambdaQuery<VolunteerReward> queryByRequest(VolunteerRewardQueryParams params) {
        return lambdaQuery()
                .eq(VolunteerReward::getVolunteerId, params.getVolunteer())
                .eq(VolunteerReward::getIssuerId, params.getIssuer())
                .in(VolunteerReward::getStatus, VolunteerRewardStatus::get, params.getStatus())
                .in(VolunteerReward::getRewardType, VolunteerRewardType::get, params.getType())
                .in(VolunteerReward::getPeriodStart, params.getTime0(), params.getTime1())
                .desc(VolunteerReward::getCreateTime);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.volunteer_reward";
    }

    @Override
    default Class<VolunteerReward> getEntityClass() {
        return VolunteerReward.class;
    }
}

