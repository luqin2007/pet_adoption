package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.AllergyHistory;
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
    default LambdaQueryWrapper<AllergyHistory> queryByRegistration(Long registrationId) {
        return lambdaQuery().eq(AllergyHistory::getRegistrationId, registrationId);
    }

    @Override
    default String getMissingMessage() {
        return "过敏史不存在";
    }
}
