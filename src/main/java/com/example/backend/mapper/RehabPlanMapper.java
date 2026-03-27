package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.dto.RehabPlanQueryRequest;
import com.example.backend.entity.RehabPlan;
import com.example.backend.entity.property.RehabPlanStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashSet;
import java.util.List;

/**
 * 索引：
 * - (petId)
 * - (doctorId)
 */
@Mapper
public interface RehabPlanMapper extends IBaseMapper<RehabPlan> {

    default LambdaQueryWrapper<RehabPlan> queryByRequest(RehabPlanQueryRequest request) {
        List<Long> users = request.getUser();
        List<Long> pets = request.getPet();
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

    default LambdaUpdateWrapper<RehabPlan> updateStatusById(Long planId, RehabPlanStatus status) {
        return lambdaUpdate()
                .eq(RehabPlan::getId, planId)
                .set(RehabPlan::getStatus, status.name());
    }

    @Override
    default String getMissingMessage() {
        return "康复计划不存在";
    }
}
