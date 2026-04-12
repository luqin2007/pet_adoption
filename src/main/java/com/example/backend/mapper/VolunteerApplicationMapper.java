package com.example.backend.mapper;

import com.example.backend.dto.VolunteerApplicationQueryParams;
import com.example.backend.entity.VolunteerApplication;
import com.example.backend.entity.property.VolunteerApplicationStatus;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (recruitmentId, userId)
 * - (status, createTime)
 * - (reviewerId, reviewTime)
 */
@Mapper
public interface VolunteerApplicationMapper extends IBaseMapper<VolunteerApplication> {

    /**
     * 根据查询条件筛选申请
     */
    default MPLambdaQuery<VolunteerApplication> queryByRequest(VolunteerApplicationQueryParams params) {
        return lambdaQuery()
                .eq(VolunteerApplication::getRecruitmentId, params.getRecruitment())
                .eq(VolunteerApplication::getUserId, params.getUser())
                .eq(VolunteerApplication::getReviewerId, params.getReviewer())
                .eq(VolunteerApplication::getProvince, params.getProvince())
                .eq(VolunteerApplication::getCity, params.getCity())
                .in(VolunteerApplication::getStatus, VolunteerApplicationStatus::get, params.getStatus())
                .desc(VolunteerApplication::getCreateTime);
    }

    /**
     * 根据招募计划和申请人查询申请
     */
    default MPLambdaQuery<VolunteerApplication> queryByRecruitmentAndUser(Long recruitmentId, Long userId) {
        return lambdaQuery()
                .eq(VolunteerApplication::getRecruitmentId, recruitmentId)
                .eq(VolunteerApplication::getUserId, userId);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.volunteer_application";
    }

    @Override
    default Class<VolunteerApplication> getEntityClass() {
        return VolunteerApplication.class;
    }
}

