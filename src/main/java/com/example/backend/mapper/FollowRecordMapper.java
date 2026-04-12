package com.example.backend.mapper;

import com.example.backend.dto.FollowRecordQueryParams;
import com.example.backend.entity.FollowRecord;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (taskId, visitTime)
 * - (volunteerId, visitTime)
 */
@Mapper
public interface FollowRecordMapper extends IBaseMapper<FollowRecord> {

    default MPLambdaQuery<FollowRecord> queryByTask(Long taskId) {
        return lambdaQuery()
                .eq(FollowRecord::getTaskId, taskId)
                .desc(FollowRecord::getVisitTime);
    }

    default MPLambdaQuery<FollowRecord> queryByTasks(Set<Long> taskIds) {
        return lambdaQuery()
                .eq(taskIds.size() == 1, FollowRecord::getTaskId, taskIds.iterator().next())
                .in(taskIds.size() != 1, FollowRecord::getTaskId, taskIds)
                .desc(FollowRecord::getVisitTime);
    }

    default MPLambdaQuery<FollowRecord> queryByRequest(FollowRecordQueryParams params) {
        return lambdaQuery()
                .eq(FollowRecord::getTaskId, params.getTask())
                .eq(FollowRecord::getVolunteerId, params.getVolunteer())
                .in(FollowRecord::getVisitTime, params.getTime0(), params.getTime1());
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.follow_record";
    }

    @Override
    default Class<FollowRecord> getEntityClass() {
        return FollowRecord.class;
    }
}

