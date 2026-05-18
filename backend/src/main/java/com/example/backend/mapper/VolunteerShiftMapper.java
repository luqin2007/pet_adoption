package com.example.backend.mapper;

import com.example.backend.dto.VolunteerShiftQueryParams;
import com.example.backend.entity.VolunteerShift;
import com.example.backend.entity.property.VolunteerShiftStatus;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;
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

    /**
     * 根据志愿者查询有效排班
     */
    default MPLambdaQuery<VolunteerShift> queryConflictByVolunteer(Long volunteerId, Date date) {
        return lambdaQuery()
                .eq(VolunteerShift::getVolunteerId, volunteerId)
                .in(VolunteerShift::getStatus, List.of( // 进行中的任务
                        VolunteerShiftStatus.ASSIGNED,
                        VolunteerShiftStatus.CONFIRMED,
                        VolunteerShiftStatus.IN_PROGRESS))
                .le(VolunteerShift::getStartTime, date) // start <= date <= end
                .ge(VolunteerShift::getEndTime, date)
                .desc(VolunteerShift::getCreateTime);
    }

    /**
     * 根据志愿者查询排班
     */
    default MPLambdaQuery<VolunteerShift> queryByVolunteerTask(Long volunteerId, Long taskId) {
        return lambdaQuery()
                .eq(VolunteerShift::getVolunteerId, volunteerId)
                .eq(VolunteerShift::getTaskId, taskId);
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

