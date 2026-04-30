package com.example.backend.mapper;

import com.example.backend.entity.RescueTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RescueTaskMapper extends IBaseMapper<RescueTask> {

    @Override
    default String getMissingMessage() {
        return "exception.not_found.rescue_task";
    }
}

