package com.example.backend.mapper;

import com.example.backend.dto.MedicalRecordQueryParams;
import com.example.backend.entity.MedicalRecord;
import com.example.backend.entity.property.MedicalRecordStatus;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：<br>
 * - (petId, status, createTime)<br>
 * - (userId, status, createTime)
 */
@Mapper
public interface MedicalRecordMapper extends IBaseMapper<MedicalRecord> {

    default MPLambdaQuery<MedicalRecord> queryByPet(Long petId) {
        return lambdaQuery().eq(MedicalRecord::getPetId, petId);
    }

    default MPLambdaQuery<MedicalRecord> queryActiveByPet(Long petId) {
        return lambdaQuery()
                .eq(MedicalRecord::getPetId, petId)
                .eq(MedicalRecord::getStatus, MedicalRecordStatus.PROCESSING);
    }

    default MPLambdaQuery<MedicalRecord> queryByOwnerId(Long ownerId) {
        return lambdaQuery().eq(MedicalRecord::getOwnerId, ownerId);
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
                .eq(MedicalRecord::getOwnerId, params.getOwnerId())
                .eq(MedicalRecord::getStatus, MedicalRecordStatus::get, params.getStatus())
                .in(MedicalRecord::getCreateTime, params.getTime0(), params.getTime1());
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.medical_record";
    }

    @Override
    default Class<MedicalRecord> getEntityClass() {
        return MedicalRecord.class;
    }
}

