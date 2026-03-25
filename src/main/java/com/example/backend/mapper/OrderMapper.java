package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

import static com.example.backend.util.C.PARENT_TREATMENT_PLAN;

/**
 * 索引：
 * - (parentId, parentType)
 */
@Mapper
public interface OrderMapper extends IBaseMapper<Order> {

    default LambdaQueryWrapper<Order> queryByTreatmentPlans(Set<Long> planIds) {
        return lambdaQuery().in(Order::getParentId, planIds).eq(Order::getParentType, PARENT_TREATMENT_PLAN);
    }

    @Override
    default String getMissingMessage() {
        return "处方不存在";
    }
}
