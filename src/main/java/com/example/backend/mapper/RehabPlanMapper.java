package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.dto.RehabPlanQueryParams;
import com.example.backend.entity.RehabPlan;
import com.example.backend.entity.property.RehabPlanStatusProp;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashSet;
import java.util.List;

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
        List<Long> users = params.getUser();
        List<Long> pets = params.getPet();
        require(users == null || pets == null, "无法同时查询 user 和 pet");

        LambdaQueryWrapper<RehabPlan> query = lambdaQuery();
        if (users != null) {
            query.eq(users.size() == 1, RehabPlan::getDoctorId, users.get(0));
            query.in(users.size() != 1, RehabPlan::getDoctorId, new HashSet<>(users));
        }
        if (pets != null) {
            query.eq(pets.size() == 1, RehabPlan::getPetId, pets.get(0));
            query.in(pets.size() != 1, RehabPlan::getPetId, new HashSet<>(pets));
        }
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
