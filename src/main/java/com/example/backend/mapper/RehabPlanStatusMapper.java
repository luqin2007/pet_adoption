package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.RehabPlanStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (planId, createTime)
 */
@Mapper
public interface RehabPlanStatusMapper extends IBaseMapper<RehabPlanStatus> {

    default LambdaQueryWrapper<RehabPlanStatus> queryByPlan(Long plan) {
        return new LambdaQueryWrapper<RehabPlanStatus>()
                .eq(RehabPlanStatus::getPlanId, plan)
                .orderByDesc(RehabPlanStatus::getCreateTime);
    }

    default LambdaQueryWrapper<RehabPlanStatus> queryByPlans(Set<Long> plans) {
        return new LambdaQueryWrapper<RehabPlanStatus>()
                .in(RehabPlanStatus::getPlanId, plans)
                .orderByDesc(RehabPlanStatus::getCreateTime);
    }
}
