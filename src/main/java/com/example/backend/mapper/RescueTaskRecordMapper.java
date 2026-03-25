package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.RescueTaskRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (taskId)
 */
@Mapper
public interface RescueTaskRecordMapper extends IBaseMapper<RescueTaskRecord> {

    /**
     * 查询任务的状态记录
     * - 索引：(taskId, userId)
     * - 查询：[RescueTaskRecord]
     *
     * @param taskId 任务 id
     */
    default LambdaQueryWrapper<RescueTaskRecord> queryByTask(Long taskId) {
        return lambdaQuery().eq(RescueTaskRecord::getTaskId, taskId);
    }

    @Override
    default String getMissingMessage() {
        return "任务状态记录不存在";
    }
}
