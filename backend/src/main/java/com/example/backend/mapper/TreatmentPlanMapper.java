package com.example.backend.mapper;

import com.example.backend.entity.TreatmentPlan;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引
 * - (detailId)
 */
@Mapper
public interface TreatmentPlanMapper extends IBaseMapper<TreatmentPlan> {

    default MPLambdaQuery<TreatmentPlan> selectByDetail(Long detailId) {
        return lambdaQuery().eq(TreatmentPlan::getDetailId, detailId);
    }

    default MPLambdaQuery<TreatmentPlan> selectByDetails(Set<Long> detailIds) {
        return lambdaQuery().in(TreatmentPlan::getDetailId, detailIds);
    }

    default MPLambdaUpdate<TreatmentPlan> discardByDetail(Long diagnosisId) {
        return lambdaUpdate()
                .eq(TreatmentPlan::getDetailId, diagnosisId)
                .set(TreatmentPlan::getIsDiscard, true);
    }

    default MPLambdaUpdate<TreatmentPlan> discardByIds(Set<Long> treatmentPlanId) {
        return lambdaUpdate()
                .in(TreatmentPlan::getId, treatmentPlanId)
                .set(TreatmentPlan::getIsDiscard, true);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.treatment_plan";
    }

    @Override
    default Class<TreatmentPlan> getEntityClass() {
        return TreatmentPlan.class;
    }
}

