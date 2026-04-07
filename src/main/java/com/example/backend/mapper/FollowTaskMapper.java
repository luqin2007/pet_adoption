package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.dto.FollowTaskQueryParams;
import com.example.backend.entity.FollowTask;
import com.example.backend.entity.property.FollowTaskStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (adoptId, status, createTime)
 * - (volunteerId, status, createTime)
 */
@Mapper
public interface FollowTaskMapper extends IBaseMapper<FollowTask> {

    default LambdaQueryWrapper<FollowTask> queryByAdopt(Long adoptId) {
        return lambdaQuery()
                .eq(FollowTask::getAdoptId, adoptId)
                .orderByDesc(FollowTask::getCreateTime);
    }

    default LambdaQueryWrapper<FollowTask> queryByAdopts(Set<Long> adoptIds) {
        return lambdaQuery()
                .eq(adoptIds.size() == 1, FollowTask::getAdoptId, adoptIds.iterator().next())
                .in(adoptIds.size() != 1, FollowTask::getAdoptId, adoptIds)
                .orderByDesc(FollowTask::getCreateTime);
    }

    default LambdaQueryWrapper<FollowTask> queryByRequest(FollowTaskQueryParams params) {
        LambdaQueryWrapper<FollowTask> query = lambdaQuery();
        params.query(query, FollowTask::getAdoptId, params.getAdopt())
                .query(query, FollowTask::getVolunteerId, params.getVolunteer())
                .querySet(query, FollowTask::getStatus, FollowTaskStatus::get, params.getStatus())
                .queryTime(query, FollowTask::getPlanTime, params.getTime0(), params.getTime1());
        return query;
    }

    @Override
    default String getMissingMessage() {
        return "跟踪任务不存在";
    }
}
