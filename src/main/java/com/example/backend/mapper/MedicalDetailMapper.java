package com.example.backend.mapper;

import com.example.backend.entity.MedicalDetail;

public interface MedicalDetailMapper extends IBaseMapper<MedicalDetail> {

    @Override
    default String getMissingMessage() {
        return "病历不存在";
    }
}
