package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.entity.TreatmentPlan;

import java.util.Set;

/**
 * 索引
 * - (detailId)
 */
public interface TreatmentPlanMapper extends IBaseMapper<TreatmentPlan> {

    default LambdaQueryWrapper<TreatmentPlan> selectByDetail(Long detailId) {
        return lambdaQuery().eq(TreatmentPlan::getDetailId, detailId);
    }

    default LambdaQueryWrapper<TreatmentPlan> selectByDetails(Set<Long> detailIds) {
        return lambdaQuery().in(TreatmentPlan::getDetailId, detailIds);
    }

    default LambdaUpdateWrapper<TreatmentPlan> discardByDetail(Long diagnosisId) {
        return lambdaUpdate()
                .eq(TreatmentPlan::getDetailId, diagnosisId)
                .set(TreatmentPlan::getIsDiscard, true);
    }

    default LambdaUpdateWrapper<TreatmentPlan> discardByIds(Set<Long> treatmentPlanId) {
        return lambdaUpdate()
                .in(TreatmentPlan::getId, treatmentPlanId)
                .set(TreatmentPlan::getIsDiscard, true);
    }

    @Override
    default String getMissingMessage() {
        return "治疗计划不存在";
    }
}
