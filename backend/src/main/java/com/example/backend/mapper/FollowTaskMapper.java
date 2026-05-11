package com.example.backend.mapper;

import com.example.backend.dto.FollowTaskQueryParams;
import com.example.backend.entity.FollowTask;
import com.example.backend.entity.property.FollowTaskStatus;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
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
        MPLambdaQuery<FollowTask> query = lambdaQuery();
        if (adoptIds.size() == 1)
            query.eq(FollowTask::getAdoptId, adoptIds.iterator().next());
        if (adoptIds.size() != 1)
            query.in(FollowTask::getAdoptId, adoptIds);
        return query.desc(FollowTask::getCreateTime);
    }

    default MPLambdaQuery<FollowTask> queryByRequest(FollowTaskQueryParams params) {
        return lambdaQuery()
                .eq(FollowTask::getAdoptId, params.getAdopt())
                .eq(FollowTask::getVolunteerId, params.getVolunteer())
                .in(FollowTask::getStatus, FollowTaskStatus::get, params.getStatus())
                .in(FollowTask::getPlanTime, params.getTime0(), params.getTime1());
    }

    default MPLambdaUpdate<FollowTask> updateNotified(Long taskId) {
        return lambdaUpdate()
                .eq(FollowTask::getId, taskId)
                .set(FollowTask::getStatus, FollowTaskStatus.NOTIFIED)
                .set(FollowTask::getUpdateTime, new Date());
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

