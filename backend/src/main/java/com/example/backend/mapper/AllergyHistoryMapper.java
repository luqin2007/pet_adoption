package com.example.backend.mapper;

import com.example.backend.entity.AllergyHistory;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：<br>
 * - (registrationId)
 */
@Mapper
public interface AllergyHistoryMapper extends IBaseMapper<AllergyHistory> {

    /**
     * 根据初诊登记获取过敏史<br>
     * - 索引：(registrationId)
     */
    default MPLambdaQuery<AllergyHistory> queryByRegistration(Long registrationId) {
        return lambdaQuery().eq(AllergyHistory::getRegistrationId, registrationId);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.allergy_history";
    }

    @Override
    default Class<AllergyHistory> getEntityClass() {
        return AllergyHistory.class;
    }
}

