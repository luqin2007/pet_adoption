package com.example.backend.mapper;

import com.example.backend.dto.RescueTaskQueryParams;
import com.example.backend.entity.RescueTask;
import com.example.backend.entity.property.RescueTaskStatus;
import com.example.backend.entity.property.RescueTaskType;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RescueTaskMapper extends IBaseMapper<RescueTask> {

    default MPLambdaQuery<RescueTask> queryByRequest(RescueTaskQueryParams params) {
        return lambdaQuery()
                .in(RescueTask::getUserId, params.getUser())
                .in(RescueTask::getStatus, RescueTaskStatus::get, params.getStatus())
                .in(RescueTask::getType, RescueTaskType::get, params.getType())
                .like(RescueTask::getSummary, params.getKeyword(), RescueTask::getDescription);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.rescue_task";
    }

    @Override
    default Class<RescueTask> getEntityClass() {
        return RescueTask.class;
    }
}

