package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.dto.FollowTaskQueryParams;
import com.example.backend.entity.FollowTask;
import com.example.backend.entity.property.FollowTaskStatus;
import com.example.backend.util.StringUtils;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Long adopt = params.getAdopt();
        Long volunteer = params.getVolunteer();
        Set<FollowTaskStatus> status = Stream.ofNullable(params.getStatus())
                .flatMap(Set::stream)
                .filter(StringUtils::hasText)
                .map(FollowTaskStatus::get)
                .collect(Collectors.toSet());
        Date time0 = params.getTime0();
        Date time1 = params.getTime1();
        require(time0 == null || time1 == null || time0.before(time1), "时间范围错误");

        return lambdaQuery()
                .eq(adopt != null, FollowTask::getAdoptId, adopt)
                .eq(volunteer != null, FollowTask::getVolunteerId, volunteer)
                // status
                .eq(status.size() == 1, FollowTask::getStatus, status.iterator().next())
                .in(status.size() > 1, FollowTask::getStatus, status)
                // planTime
                .ge(time0 != null && time1 == null, FollowTask::getPlanTime, time0)
                .le(time0 == null && time1 != null, FollowTask::getPlanTime, time1)
                .in(time0 != null && time1 != null, FollowTask::getPlanTime, time0, time1);
    }

    @Override
    default String getMissingMessage() {
        return "跟踪任务不存在";
    }
}
