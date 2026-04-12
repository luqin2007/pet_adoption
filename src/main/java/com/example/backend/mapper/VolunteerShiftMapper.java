package com.example.backend.mapper;

import com.example.backend.dto.VolunteerShiftQueryParams;
import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.property.VolunteerShiftStatus;
import com.example.backend.entity.property.VolunteerTaskType;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (volunteerId, startTime, endTime)
 * - (status, startTime)
 * - (taskType, taskSourceId)
 */
@Mapper
public interface VolunteerShiftMapper extends IBaseMapper<VolunteerShift> {

    /**
     * 根据查询条件筛选排班
     */
    default MPLambdaQuery<VolunteerShift> queryByRequest(VolunteerShiftQueryParams params) {
        return lambdaQuery()
                .eq(VolunteerShift::getVolunteerId, params.getVolunteer())
                .eq(VolunteerShift::getAssignerId, params.getAssigner())
                .in(VolunteerShift::getTaskType, VolunteerTaskType::get, params.getTaskType())
                .in(VolunteerShift::getStatus, VolunteerShiftStatus::get, params.getStatus())
                .in(VolunteerShift::getStartTime, params.getTime0(), params.getTime1())
                .like(VolunteerShift::getTitle, params.getKeyword())
                .desc(VolunteerShift::getStartTime);
    }

    /**
     * 根据志愿者查询排班
     */
    default MPLambdaQuery<VolunteerShift> queryByVolunteer(Long volunteerId) {
        return lambdaQuery()
                .eq(VolunteerShift::getVolunteerId, volunteerId)
                .desc(VolunteerShift::getStartTime);
    }

    @Override
    default String getMissingMessage() {
        return "志愿者排班不存在";
    }

    @Override
    default Class<VolunteerShift> getEntityClass() {
        return VolunteerShift.class;
    }
}
