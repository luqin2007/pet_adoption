package com.example.backend.mapper;

import com.example.backend.entity.Order;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.MPLambdaQuery;
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
    default MPLambdaQuery<Order> queryByTreatmentPlans(Set<Long> planIds) {
        return lambdaQuery()
                .eq(Order::getParentType, ParentType.TREATMENT_PLAN)
                .in(Order::getParentId, planIds);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.order";
    }

    @Override
    default Class<Order> getEntityClass() {
        return Order.class;
    }
}

