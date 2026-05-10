package com.example.backend.mapper;

import com.example.backend.dto.VolunteerRecruitmentQueryParams;
import com.example.backend.entity.VolunteerRecruitment;
import com.example.backend.entity.property.VolunteerRecruitmentStatus;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (status, startTime, endTime)
 * - (publisherId, createTime)
 */
@Mapper
public interface VolunteerRecruitmentMapper extends IBaseMapper<VolunteerRecruitment> {

    /**
     * 根据查询条件筛选招募计划
     */
    default MPLambdaQuery<VolunteerRecruitment> queryByRequest(VolunteerRecruitmentQueryParams params) {
        return lambdaQuery()
                .eq(VolunteerRecruitment::getPublisherId, params.getPublisher())
                .in(VolunteerRecruitment::getStatus, VolunteerRecruitmentStatus::get, params.getStatus())
                .in(VolunteerRecruitment::getStartTime, params.getTime0(), params.getTime1())
                .like(VolunteerRecruitment::getTitle, params.getTitle())
                .desc(VolunteerRecruitment::getCreateTime);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.volunteer_recruitment";
    }

    @Override
    default Class<VolunteerRecruitment> getEntityClass() {
        return VolunteerRecruitment.class;
    }
}

