package com.example.backend.mapper;

import com.example.backend.entity.ImmunityHistory;
import com.example.backend.util.MPLambdaQuery;

/**
 * 索引：<br>
 * - (registrationId)
 */
public interface ImmunityHistoryMapper extends IBaseMapper<ImmunityHistory> {

    /**
     * 根据初诊登记获取过敏史<br>
     * - 索引：(registrationId)
     */
    default MPLambdaQuery<ImmunityHistory> queryByRegistration(Long registrationId) {
        return lambdaQuery().eq(ImmunityHistory::getRegistrationId, registrationId);
    }

    @Override
    default String getMissingMessage() {
        return "免疫史不存在";
    }

    @Override
    default Class<ImmunityHistory> getEntityClass() {
        return ImmunityHistory.class;
    }
}
