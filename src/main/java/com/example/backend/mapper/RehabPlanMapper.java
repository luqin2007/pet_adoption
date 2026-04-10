package com.example.backend.mapper;

import com.example.backend.dto.RehabPlanQueryParams;
import com.example.backend.entity.RehabPlan;
import com.example.backend.entity.property.RehabPlanStatusProp;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：<br>
 * - (petId)<br>
 * - (doctorId)
 */
@Mapper
public interface RehabPlanMapper extends IBaseMapper<RehabPlan> {

    /**
     * 根据查询条件查询康复计划<br>
     * - 索引：(petId)<br>
     * - 索引：(doctorId)
     */
    default MPLambdaQuery<RehabPlan> queryByRequest(RehabPlanQueryParams params) {
        return lambdaQuery()
                .in(RehabPlan::getDoctorId, params.getDoctor())
                .in(RehabPlan::getPetId, params.getPet());
    }

    /**
     * 更新康复计划状态
     */
    default MPLambdaUpdate<RehabPlan> updateStatusById(Long planId, RehabPlanStatusProp status) {
        return lambdaUpdate()
                .eq(RehabPlan::getId, planId)
                .set(RehabPlan::getStatus, status.name());
    }

    @Override
    default String getMissingMessage() {
        return "康复计划不存在";
    }

    @Override
    default Class<RehabPlan> getEntityClass() {
        return RehabPlan.class;
    }
}
