package com.example.backend.mapper;

import com.example.backend.entity.RescueTaskAssign;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：<br>
 * - (taskId, userId)
 */
@Mapper
public interface RescueTaskAssignMapper extends IBaseMapper<RescueTaskAssign> {

    /**
     * 查询任务的执行人<br>
     * - 索引：(taskId, userId)
     */
    default MPLambdaQuery<RescueTaskAssign> queryUserByTask(Long taskId) {
        return lambdaQuery()
                .eq(RescueTaskAssign::getTaskId, taskId);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.rescue_task_assign";
    }

    @Override
    default Class<RescueTaskAssign> getEntityClass() {
        return RescueTaskAssign.class;
    }
}

