package com.example.backend.mapper;

import com.example.backend.dto.VolunteerShiftQueryParams;
import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.property.VolunteerShiftStatus;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (volunteerId, createTime)
 * - (status, createTime)
 */
@Mapper
public interface VolunteerShiftMapper extends IBaseMapper<VolunteerShift> {

    /**
     * 根据查询条件筛选排班
     */
    default MPLambdaQuery<VolunteerShift> queryByRequest(VolunteerShiftQueryParams params) {
        return queryByRequest(params, null);
    }

    /**
     * 根据查询条件和任务记录筛选排班
     */
    default MPLambdaQuery<VolunteerShift> queryByRequest(VolunteerShiftQueryParams params, Set<Long> taskRecordIds) {
        return lambdaQuery()
                .eq(VolunteerShift::getVolunteerId, params.getVolunteer())
                .eq(VolunteerShift::getAssignerId, params.getAssigner())
                .in(VolunteerShift::getTaskId, taskRecordIds)
                .in(VolunteerShift::getStatus, VolunteerShiftStatus::get, params.getStatus())
                .desc(VolunteerShift::getCreateTime);
    }

    /**
     * 根据志愿者查询排班
     */
    default MPLambdaQuery<VolunteerShift> queryByVolunteer(Long volunteerId) {
        return lambdaQuery()
                .eq(VolunteerShift::getVolunteerId, volunteerId)
                .desc(VolunteerShift::getCreateTime);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.volunteer_shift";
    }

    @Override
    default Class<VolunteerShift> getEntityClass() {
        return VolunteerShift.class;
    }
}

