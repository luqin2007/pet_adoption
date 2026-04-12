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
        return lambdaQuery()
                .in(VolunteerShiftStatusRecord::getShiftId, ids)
                .desc(VolunteerShiftStatusRecord::getCreateTime);
    }
}
