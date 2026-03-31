package com.example.backend.mapper;

import com.example.backend.entity.AgreementUpdateRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgreementUpdateRecordMapper extends IBaseMapper<AgreementUpdateRecord> {

    @Override
    default String getMissingMessage() {
        return "协议更新记录不存在";
    }
}
