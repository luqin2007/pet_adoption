package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.HealthAssessment;

/**
 * 索引
 * - (petId, createTime)
 */
public interface HealthAssessmentMapper extends IBaseMapper<HealthAssessment> {

    default LambdaQueryWrapper<HealthAssessment> queryByPet(Long petId) {
        return lambdaQuery()
                .eq(HealthAssessment::getPetId, petId)
                .orderByDesc(HealthAssessment::getCreateTime);
    }
}
