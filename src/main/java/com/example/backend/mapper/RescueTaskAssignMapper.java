package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.RescueTaskAssign;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (taskId, userId)
 */
@Mapper
public interface RescueTaskAssignMapper extends IBaseMapper<RescueTaskAssign> {

    /**
     * 查询任务的执行人
     * - 索引：(taskId, userId)
     * - 查询：[RescueTaskAssign(userId)]
     *
     * @param taskId 任务 id
     */
    default LambdaQueryWrapper<RescueTaskAssign> queryUserIdByTask(Long taskId) {
        return lambdaQuery()
                .eq(RescueTaskAssign::getTaskId, taskId)
                .select(RescueTaskAssign::getUserId);
    }

    @Override
    default String getMissingMessage() {
        return "任务分配记录不存在";
    }
}
