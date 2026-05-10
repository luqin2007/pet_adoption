package com.example.backend.mapper;

import com.example.backend.dto.VolunteerServiceRecordQueryParams;
import com.example.backend.entity.VolunteerServiceRecord;
import com.example.backend.entity.property.VolunteerRecordStatus;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (shiftId)
 * - (volunteerId, startTime)
 * - (status, reviewTime)
 */
@Mapper
public interface VolunteerServiceRecordMapper extends IBaseMapper<VolunteerServiceRecord> {

    /**
     * 根据排班查询服务记录
     */
    default MPLambdaQuery<VolunteerServiceRecord> queryByShift(Long shiftId) {
        return lambdaQuery().eq(VolunteerServiceRecord::getShiftId, shiftId);
    }

    /**
     * 根据排班集合查询服务记录
     */
    default MPLambdaQuery<VolunteerServiceRecord> queryByShifts(Set<Long> shiftIds) {
        return lambdaQuery().in(VolunteerServiceRecord::getShiftId, shiftIds);
    }

    /**
     * 根据查询条件筛选服务记录
     */
    default MPLambdaQuery<VolunteerServiceRecord> queryByRequest(VolunteerServiceRecordQueryParams params) {
        return lambdaQuery()
                .eq(VolunteerServiceRecord::getVolunteerId, params.getVolunteer())
                .eq(VolunteerServiceRecord::getShiftId, params.getShift())
                .eq(VolunteerServiceRecord::getReviewerId, params.getReviewer())
                .in(VolunteerServiceRecord::getStatus, VolunteerRecordStatus::get, params.getStatus())
                .in(VolunteerServiceRecord::getStartTime, params.getTime0(), params.getTime1())
                .desc(VolunteerServiceRecord::getCreateTime);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.volunteer_service_record";
    }

    @Override
    default Class<VolunteerServiceRecord> getEntityClass() {
        return VolunteerServiceRecord.class;
    }
}

