package com.example.backend.mapper;

import com.example.backend.dto.AgreementQueryParams;
import com.example.backend.entity.Agreement;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * 索引：
 * - (parentType, parentId, signed, createTime)
 */
@Mapper
public interface AgreementMapper extends IBaseMapper<Agreement> {

    default MPLambdaQuery<Agreement> queryByRequest(AgreementQueryParams params) {
        return lambdaQuery()
                .eq(Agreement::getParentType, params.getParentType())
                .eq(Agreement::getParentId, params.getParentId())
                .eq(Agreement::getApplicantId, params.getUser())
                .exist(Agreement::getSignTime, params.getSigned())
                .in(Agreement::getCreateTime, params.getTime0(), params.getTime1());
    }

    default MPLambdaUpdate<Agreement> setUpdateTime(Long agreementId) {
        return lambdaUpdate()
                .eq(Agreement::getId, agreementId)
                .set(Agreement::getUpdateTime, new Date());
    }

    default MPLambdaUpdate<Agreement> uploadSign(Long agreementId, String filename, Date now) {
        return lambdaUpdate()
                .eq(Agreement::getId, agreementId)
                .set(Agreement::getUpdateTime, now)
                .set(Agreement::getSignTime, null)
                .set(Agreement::getSign, filename);
    }

    default MPLambdaUpdate<Agreement> confirmSign(Long agreementId, Date now) {
        return lambdaUpdate()
                .eq(Agreement::getId, agreementId)
                .set(Agreement::getUpdateTime, now)
                .set(Agreement::getSignTime, now);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.agreement";
    }

    @Override
    default Class<Agreement> getEntityClass() {
        return Agreement.class;
    }
}

