package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.RescueTaskRecord;
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
    default LambdaQueryWrapper<RescueTaskRecord> queryByTask(Long taskId) {
        return lambdaQuery().eq(RescueTaskRecord::getTaskId, taskId);
    }

    @Override
    default String getMissingMessage() {
        return "任务状态记录不存在";
    }
}
