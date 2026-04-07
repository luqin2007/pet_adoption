package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.dto.FollowRecordQueryParams;
import com.example.backend.entity.FollowRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (taskId, visitTime)
 * - (volunteerId, visitTime)
 */
@Mapper
public interface FollowRecordMapper extends IBaseMapper<FollowRecord> {

    default LambdaQueryWrapper<FollowRecord> queryByTask(Long taskId) {
        return lambdaQuery()
                .eq(FollowRecord::getTaskId, taskId)
                .orderByDesc(FollowRecord::getVisitTime);
    }

    default LambdaQueryWrapper<FollowRecord> queryByTasks(Set<Long> taskIds) {
        return lambdaQuery()
                .eq(taskIds.size() == 1, FollowRecord::getTaskId, taskIds.iterator().next())
                .in(taskIds.size() != 1, FollowRecord::getTaskId, taskIds)
                .orderByDesc(FollowRecord::getVisitTime);
    }

    default LambdaQueryWrapper<FollowRecord> queryByRequest(FollowRecordQueryParams params) {
        LambdaQueryWrapper<FollowRecord> query = lambdaQuery();
        params.query(query, FollowRecord::getTaskId, params.getTask())
                .query(query, FollowRecord::getVolunteerId, params.getVolunteer())
                .queryTime(query, FollowRecord::getVisitTime, params.getTime0(), params.getTime1());
        return query;
    }

    @Override
    default String getMissingMessage() {
        return "跟踪记录不存在";
    }
}
