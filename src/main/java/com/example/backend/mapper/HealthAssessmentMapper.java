package com.example.backend.mapper;

import com.example.backend.entity.HealthAssessment;
import com.example.backend.util.MPLambdaQuery;

/**
 * 索引<br>
 * - (petId, createTime)
 */
public interface HealthAssessmentMapper extends IBaseMapper<HealthAssessment> {

    /**
     * 根据宠物获取健康评估<br>
     * - 索引：(petId, createTime)
     */
    default MPLambdaQuery<HealthAssessment> queryByPet(Long petId) {
        return lambdaQuery()
                .eq(HealthAssessment::getPetId, petId)
                .desc(HealthAssessment::getCreateTime);
    }

    @Override
    default String getMissingMessage() {
        return "健康评估不存在";
    }

    @Override
    default Class<HealthAssessment> getEntityClass() {
        return HealthAssessment.class;
    }
}
