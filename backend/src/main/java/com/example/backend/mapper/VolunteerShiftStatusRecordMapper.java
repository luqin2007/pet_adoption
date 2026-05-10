package com.example.backend.mapper;

import com.example.backend.entity.VolunteerShiftStatusRecord;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引
 * - (shiftId, createTime)
 */
@Mapper
public interface VolunteerShiftStatusRecordMapper extends IBaseMapper<VolunteerShiftStatusRecord> {

    default MPLambdaQuery<VolunteerShiftStatusRecord> queryByShift(Long id) {
        return lambdaQuery()
                .eq(VolunteerShiftStatusRecord::getShiftId, id)
                .desc(VolunteerShiftStatusRecord::getCreateTime);
    }

    default MPLambdaQuery<VolunteerShiftStatusRecord> queryByShifts(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return lambdaQuery().eq(VolunteerShiftStatusRecord::getId, Long.MIN_VALUE);
        }
        return lambdaQuery()
                .in(VolunteerShiftStatusRecord::getShiftId, ids)
                .desc(VolunteerShiftStatusRecord::getCreateTime);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.volunteer.shift_status_record";
    }

    @Override
    default Class<VolunteerShiftStatusRecord> getEntityClass() {
        return VolunteerShiftStatusRecord.class;
    }
}
