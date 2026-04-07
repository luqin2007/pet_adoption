package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.dto.MedicalRecordQueryParams;
import com.example.backend.entity.MedicalRecord;
import com.example.backend.entity.property.MedicalRecordStatus;

/**
 * 索引：<br>
 * - (petId, status, createTime)<br>
 * - (userId, status, createTime)
 */
public interface MedicalRecordMapper extends IBaseMapper<MedicalRecord> {

    default LambdaQueryWrapper<MedicalRecord> selectByPet(Long petId) {
        return lambdaQuery()
                .eq(MedicalRecord::getPetId, petId);
    }

    /**
     * 索引：<br>
     * - (petId, status, createTime)<br>
     * - (userId, status, createTime)
     */
    default LambdaQueryWrapper<MedicalRecord> queryByRequest(MedicalRecordQueryParams params) {
        LambdaQueryWrapper<MedicalRecord> query = lambdaQuery();
        params.query(query, MedicalRecord::getPetId, params.getPet())
                .query(query, MedicalRecord::getDoctorId, params.getDoctor())
                .query(query, MedicalRecord::getStatus, MedicalRecordStatus::get, params.getStatus())
                .queryTime(query, MedicalRecord::getCreateTime, params.getTime0(), params.getTime1());
        return query;
    }

    @Override
    default String getMissingMessage() {
        return "就诊记录不存在";
    }
}
