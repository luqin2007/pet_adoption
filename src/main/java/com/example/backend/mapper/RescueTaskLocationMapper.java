package com.example.backend.mapper;

import com.example.backend.entity.Location;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：<br>
 * - (parentId)
 */
@Mapper
public interface RescueTaskLocationMapper extends IBaseMapper<Location> {

    /**
     * 查询指定任务的位置<br>
     * - 索引：(parentId)
     */
    default MPLambdaQuery<Location> queryByTask(Long taskId) {
        return lambdaQuery().eq(Location::getParentId, taskId);
    }

    @Override
    default String getMissingMessage() {
        return "救助位置不存在";
    }

    @Override
    default Class<Location> getEntityClass() {
        return Location.class;
    }
}
