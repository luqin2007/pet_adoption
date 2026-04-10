package com.example.backend.mapper;

import com.example.backend.dto.MedicalRecordQueryParams;
import com.example.backend.entity.MedicalRecord;
import com.example.backend.entity.property.MedicalRecordStatus;
import com.example.backend.util.MPLambdaQuery;

/**
 * 索引：<br>
 * - (petId, status, createTime)<br>
 * - (userId, status, createTime)
 */
public interface MedicalRecordMapper extends IBaseMapper<MedicalRecord> {

    default MPLambdaQuery<MedicalRecord> selectByPet(Long petId) {
        return lambdaQuery().eq(MedicalRecord::getPetId, petId);
    }

    /**
     * 索引：<br>
     * - (petId, status, createTime)<br>
     * - (userId, status, createTime)
     */
    default MPLambdaQuery<MedicalRecord> queryByRequest(MedicalRecordQueryParams params) {
        return lambdaQuery()
                .eq(MedicalRecord::getPetId, params.getPet())
                .eq(MedicalRecord::getDoctorId, params.getDoctor())
                .eq(MedicalRecord::getStatus, MedicalRecordStatus::get, params.getStatus())
                .in(MedicalRecord::getCreateTime, params.getTime0(), params.getTime1());
    }

    @Override
    default String getMissingMessage() {
        return "就诊记录不存在";
    }

    @Override
    default Class<MedicalRecord> getEntityClass() {
        return MedicalRecord.class;
    }
}
