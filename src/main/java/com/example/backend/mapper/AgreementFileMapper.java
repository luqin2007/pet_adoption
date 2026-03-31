package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.AgreementFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

@Mapper
public interface AgreementFileMapper extends IBaseMapper<AgreementFile> {

    default LambdaQueryWrapper<AgreementFile> queryByAgreement(Long agreementId) {
        return lambdaQuery().eq(AgreementFile::getAgreementId, agreementId);
    }

    default LambdaQueryWrapper<AgreementFile> queryByAgreements(Set<Long> agreementIds) {
        return lambdaQuery().in(AgreementFile::getAgreementId, agreementIds);
    }

    @Override
    default String getMissingMessage() {
        return "协议文件不存在";
    }
}
