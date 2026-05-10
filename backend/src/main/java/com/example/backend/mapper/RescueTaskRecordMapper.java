package com.example.backend.mapper;

import com.example.backend.entity.RescueTaskRecord;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：<br>
 * - (taskId)
 */
@Mapper
public interface RescueTaskRecordMapper extends IBaseMapper<RescueTaskRecord> {

    /**
     * 根据救助任务查询任务的状态记录<br>
     * - 索引：(taskId)
     */
    default MPLambdaQuery<RescueTaskRecord> queryByTask(Long taskId) {
        return lambdaQuery().eq(RescueTaskRecord::getTaskId, taskId);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.rescue_task_record";
    }

    @Override
    default Class<RescueTaskRecord> getEntityClass() {
        return RescueTaskRecord.class;
    }
}

