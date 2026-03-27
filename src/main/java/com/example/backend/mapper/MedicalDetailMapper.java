package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.MedicalDetail;

import java.util.HashSet;
import java.util.List;

/**
 * 索引：<br>
 * - (recordId, createTime)<br>
 * - (doctorId, createTime)
 */
public interface MedicalDetailMapper extends IBaseMapper<MedicalDetail> {

    /**
     * 根据病历或医生查询<br>
     * - 索引：(recordId, createTime)<br>
     * - 索引：(doctorId, createTime)
     */
    default LambdaQueryWrapper<MedicalDetail> queryByParams(MedicalDetailQueryParams params) {
        List<Long> records = params.getRecord();
        List<Long> users = params.getUser();
        LambdaQueryWrapper<MedicalDetail> query = lambdaQuery();
        if (records != null) {
            query.in(MedicalDetail::getRecordId, new HashSet<>(records));
        }
        if (users != null){
            query.in(MedicalDetail::getDoctorId, new HashSet<>(users));
        }
        return query.orderByDesc(MedicalDetail::getCreateTime);
    }

    @Override
    default String getMissingMessage() {
        return "病历不存在";
    }
}
