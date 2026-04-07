package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.dto.RehabPlanQueryParams;
import com.example.backend.entity.RehabPlan;
import com.example.backend.entity.property.RehabPlanStatusProp;
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
    default LambdaQueryWrapper<RehabPlan> queryByRequest(RehabPlanQueryParams params) {
        LambdaQueryWrapper<RehabPlan> query = lambdaQuery();
        params.querySet(query, RehabPlan::getDoctorId, params.getDoctor())
                .querySet(query, RehabPlan::getPetId, params.getPet());
        return query;
    }

    /**
     * 更新康复计划状态
     */
    default LambdaUpdateWrapper<RehabPlan> updateStatusById(Long planId, RehabPlanStatusProp status) {
        return lambdaUpdate()
                .eq(RehabPlan::getId, planId)
                .set(RehabPlan::getStatus, status.name());
    }

    @Override
    default String getMissingMessage() {
        return "康复计划不存在";
    }
}
