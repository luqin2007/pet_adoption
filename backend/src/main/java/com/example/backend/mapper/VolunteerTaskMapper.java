package com.example.backend.mapper;

import com.example.backend.entity.VolunteerTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VolunteerTaskMapper extends IBaseMapper<VolunteerTask> {

    @Override
    default String getMissingMessage() {
        return "exception.not_found.volunteer_task";
    }

    @Override
    default Class<VolunteerTask> getEntityClass() {
        return VolunteerTask.class;
    }
}
