package com.example.backend.mapper;

import com.example.backend.dto.VolunteerProfileQueryParams;
import com.example.backend.entity.VolunteerProfile;
import com.example.backend.entity.property.VolunteerProfileStatus;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (userId)
 * - (status, joinTime)
 */
@Mapper
public interface VolunteerProfileMapper extends IBaseMapper<VolunteerProfile> {

    /**
     * 根据查询条件筛选志愿者档案
     */
    default MPLambdaQuery<VolunteerProfile> queryByRequest(VolunteerProfileQueryParams params) {
        return lambdaQuery()
                .eq(VolunteerProfile::getUserId, params.getUser())
                .in(VolunteerProfile::getStatus, VolunteerProfileStatus::get, params.getStatus())
                .like(VolunteerProfile::getRealName, params.getKeyword())
                .desc(VolunteerProfile::getCreateTime);
    }

    /**
     * 根据用户 id 查询志愿者档案
     */
    default MPLambdaQuery<VolunteerProfile> queryByUserId(Long userId) {
        return lambdaQuery().eq(VolunteerProfile::getUserId, userId);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.volunteer_profile";
    }

    @Override
    default Class<VolunteerProfile> getEntityClass() {
        return VolunteerProfile.class;
    }
}

