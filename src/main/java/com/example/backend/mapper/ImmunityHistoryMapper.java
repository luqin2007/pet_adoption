package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.ImmunityHistory;

/**
 * 索引：
 * - (registrationId)
 */
public interface ImmunityHistoryMapper extends IBaseMapper<ImmunityHistory> {

    default LambdaQueryWrapper<ImmunityHistory> queryByRegistration(Long registrationId) {
        return lambdaQuery().eq(ImmunityHistory::getRegistrationId, registrationId);
    }

    @Override
    default String getMissingMessage() {
        return "免疫史不存在";
    }
}
