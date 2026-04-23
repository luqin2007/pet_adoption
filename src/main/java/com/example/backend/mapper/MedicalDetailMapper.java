package com.example.backend.mapper;

import com.example.backend.dto.MedicalDetailQueryParams;
import com.example.backend.entity.MedicalDetail;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：<br>
 * - (recordId, createTime)<br>
 * - (doctorId, createTime)
 */
@Mapper
public interface MedicalDetailMapper extends IBaseMapper<MedicalDetail> {

    /**
     * 根据病历或医生查询<br>
     * - 索引：(recordId, createTime)<br>
     * - 索引：(doctorId, createTime)
     */
    default MPLambdaQuery<MedicalDetail> queryByParams(MedicalDetailQueryParams params) {
        return lambdaQuery()
                .in(MedicalDetail::getRecordId, params.getRecord())
                .in(MedicalDetail::getDoctorId, params.getUser())
                .desc(MedicalDetail::getCreateTime);
    }

    /**
     * 根据病历或医生查询<br>
     * - 索引：(recordId, createTime)<br>
     * - 索引：(doctorId, createTime)
     */
    default MPLambdaQuery<MedicalDetail> queryByRecord(Long recordId) {
        return lambdaQuery().eq(MedicalDetail::getRecordId, recordId);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.medical_detail";
    }

    @Override
    default Class<MedicalDetail> getEntityClass() {
        return MedicalDetail.class;
    }
}

