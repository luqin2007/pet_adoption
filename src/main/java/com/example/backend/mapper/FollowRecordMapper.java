package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.dto.FollowRecordQueryParams;
import com.example.backend.entity.FollowRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
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
        Long task = params.getTask();
        Long volunteer = params.getVolunteer();
        Date time0 = params.getTime0();
        Date time1 = params.getTime1();

        return lambdaQuery()
                .eq(task != null, FollowRecord::getTaskId, task)
                .eq(volunteer != null, FollowRecord::getVolunteerId, volunteer)
                .in(time0 != null && time1 != null, FollowRecord::getVisitTime, time0, time1)
                .ge(time0 != null && time1 == null, FollowRecord::getVisitTime, time0)
                .le(time0 == null && time1 != null, FollowRecord::getVisitTime, time1);
    }

    @Override
    default String getMissingMessage() {
        return "跟踪记录不存在";
    }
}
