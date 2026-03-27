package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.HealthAssessment;

/**
 * 索引<br>
 * - (petId, createTime)
 */
public interface HealthAssessmentMapper extends IBaseMapper<HealthAssessment> {

    /**
     * 根据宠物获取健康评估<br>
     * - 索引：(petId, createTime)
     */
    default LambdaQueryWrapper<HealthAssessment> queryByPet(Long petId) {
        return lambdaQuery()
                .eq(HealthAssessment::getPetId, petId)
                .orderByDesc(HealthAssessment::getCreateTime);
    }
}
