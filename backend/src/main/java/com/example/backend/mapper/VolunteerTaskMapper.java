package com.example.backend.mapper;

import com.example.backend.entity.VolunteerTask;
import com.example.backend.entity.property.VolunteerTaskType;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VolunteerTaskMapper extends IBaseMapper<VolunteerTask> {

    default MPLambdaQuery<VolunteerTask> queryFollowTask(Long taskId) {
        return lambdaQuery()
                .eq(VolunteerTask::getTaskType, VolunteerTaskType.FOLLOW_VISIT)
                .eq(VolunteerTask::getTaskId, taskId);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.volunteer_task";
    }

    @Override
    default Class<VolunteerTask> getEntityClass() {
        return VolunteerTask.class;
    }
}
