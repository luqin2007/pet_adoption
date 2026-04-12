package com.example.backend.mapper;

import com.example.backend.dto.FollowTaskQueryParams;
import com.example.backend.entity.FollowTask;
import com.example.backend.entity.property.FollowTaskStatus;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (adoptId, status, createTime)
 * - (volunteerId, status, createTime)
 */
@Mapper
public interface FollowTaskMapper extends IBaseMapper<FollowTask> {

    default MPLambdaQuery<FollowTask> queryByAdopt(Long adoptId) {
        return lambdaQuery()
                .eq(FollowTask::getAdoptId, adoptId)
                .desc(FollowTask::getCreateTime);
    }

    default MPLambdaQuery<FollowTask> queryByAdopts(Set<Long> adoptIds) {
        return lambdaQuery()
                .eq(adoptIds.size() == 1, FollowTask::getAdoptId, adoptIds.iterator().next())
                .in(adoptIds.size() != 1, FollowTask::getAdoptId, adoptIds)
                .desc(FollowTask::getCreateTime);
    }

    default MPLambdaQuery<FollowTask> queryByRequest(FollowTaskQueryParams params) {
        return lambdaQuery()
                .eq(FollowTask::getAdoptId, params.getAdopt())
                .eq(FollowTask::getVolunteerId, params.getVolunteer())
                .in(FollowTask::getStatus, FollowTaskStatus::get, params.getStatus())
                .in(FollowTask::getPlanTime, params.getTime0(), params.getTime1());
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.follow_task";
    }

    @Override
    default Class<FollowTask> getEntityClass() {
        return FollowTask.class;
    }
}

