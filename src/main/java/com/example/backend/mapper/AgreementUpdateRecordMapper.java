package com.example.backend.mapper;

import com.example.backend.entity.AgreementUpdateRecord;
import com.example.backend.entity.property.AgreementUpdateStatus;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgreementUpdateRecordMapper extends IBaseMapper<AgreementUpdateRecord> {

    default MPLambdaUpdate<AgreementUpdateRecord> updateStatus(Long recordId, AgreementUpdateStatus status) {
        return lambdaUpdate()
                .eq(AgreementUpdateRecord::getId, recordId)
                .set(AgreementUpdateRecord::getStatus, status);
    }

    @Override
    default String getMissingMessage() {
        return "协议更新记录不存在";
    }
}
