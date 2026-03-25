package com.example.backend.mapper;

import com.example.backend.entity.RescueTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RescueTaskMapper extends IBaseMapper<RescueTask> {

    @Override
    default String getMissingMessage() {
        return "救助任务不存在";
    }
}
