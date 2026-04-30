package com.example.backend.mapper;

import com.example.backend.entity.AgreementFile;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

@Mapper
public interface AgreementFileMapper extends IBaseMapper<AgreementFile> {

    default MPLambdaQuery<AgreementFile> queryByAgreement(Long agreementId) {
        return lambdaQuery()
                .eq(AgreementFile::getAgreementId, agreementId)
                .asc(AgreementFile::getPage);
    }

    default MPLambdaQuery<AgreementFile> queryByAgreements(Set<Long> agreementIds) {
        if (agreementIds == null || agreementIds.isEmpty()) {
            return lambdaQuery().eq(AgreementFile::getId, Long.MIN_VALUE);
        }
        return lambdaQuery().in(AgreementFile::getAgreementId, agreementIds);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.agreement_file";
    }

    @Override
    default Class<AgreementFile> getEntityClass() {
        return AgreementFile.class;
    }
}
