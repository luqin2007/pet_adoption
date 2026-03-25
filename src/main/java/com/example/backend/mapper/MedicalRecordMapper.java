package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.dto.MedicalRecordQueryRequest;
import com.example.backend.entity.MedicalRecord;
import com.example.backend.util.ServiceException;

import java.util.Date;

/**
 * 索引：
 * - (petId, status, createTime)
 * - (userId, status, createTime)
 */
public interface MedicalRecordMapper extends IBaseMapper<MedicalRecord> {

    default LambdaQueryWrapper<MedicalRecord> selectByPet(Long petId) {
        return lambdaQuery()
            .eq(MedicalRecord::getPetId, petId);
    }

    /**
     * 索引：
     * - (petId, type, createTime)
     * - (userId, type, createTime)
     */
    default LambdaQueryWrapper<MedicalRecord> queryByRequest(MedicalRecordQueryRequest request) {
        // 校验查询参数：pet, user 互斥
        Long pet = request.getPet();
        Long doctor = request.getDoctor();
        Integer status = request.getStatus();
        Date time0 = request.getTime0(), time1 = request.getTime1();
        if (pet != null && doctor != null)
            throw ServiceException.invalidate("无法同时查询宠物 id 与接诊人");

        LambdaQueryWrapper<MedicalRecord> query = lambdaQuery();
        query.eq(pet != null, MedicalRecord::getPetId, pet);
        query.eq(doctor != null, MedicalRecord::getDoctorId, doctor);
        query.eq(status != null, MedicalRecord::getStatus, status);
        query.in(time0 != null && time1 != null, MedicalRecord::getCreateTime, time0, time1);
        query.le(time0 == null && time1 != null, MedicalRecord::getCreateTime, time1);
        query.ge(time0 != null && time1 == null, MedicalRecord::getCreateTime, time0);
        return query;
    }

    @Override
    default String getMissingMessage() {
        return "就诊记录不存在";
    }
}
