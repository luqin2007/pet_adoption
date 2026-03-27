package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.Order;
import com.example.backend.entity.property.ParentType;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：<br>
 * - (parentType, parentId)
 */
@Mapper
public interface OrderMapper extends IBaseMapper<Order> {

    /**
     * 查询指定治疗计划下的所有处方<br>
     * - 索引：(parentType, parentId)
     */
    default LambdaQueryWrapper<Order> queryByTreatmentPlans(Set<Long> planIds) {
        return lambdaQuery()
                .eq(Order::getParentType, ParentType.TREATMENT_PLAN)
                .in(Order::getParentId, planIds);
    }

    @Override
    default String getMissingMessage() {
        return "处方不存在";
    }
}
