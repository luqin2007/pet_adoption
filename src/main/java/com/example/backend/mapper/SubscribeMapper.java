package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.dto.SubscribeQueryParams;
import com.example.backend.entity.Subscribe;
import com.example.backend.entity.property.SubscribeAction;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (action, elementId)
 * - (userId, action, elementId)
 */
@Mapper
public interface SubscribeMapper extends IBaseMapper<Subscribe> {

    default LambdaQueryWrapper<Subscribe> deleteUserSubscribes(Set<Long> subscribeIds, Long id) {
        return new LambdaQueryWrapper<Subscribe>()
                .in(Subscribe::getId, subscribeIds)
                .eq(Subscribe::getUserId, id);
    }

    default MPLambdaQuery<Subscribe> queryByRequest(SubscribeQueryParams params) {
        return lambdaQuery()
                .in(Subscribe::getUserId, params.getUser())
                .in(Subscribe::getAction, SubscribeAction::get, params.getAction())
                .in(Subscribe::getElementId, params.getElement())
                .in(Subscribe::getCount, params.getMin(), params.getMax())
                .in(Subscribe::getCreateTime, params.getTime0(), params.getTime1());
    }

    @Override
    default String getMissingMessage() {
        return "订阅不存在";
    }

    @Override
    default Class<Subscribe> getEntityClass() {
        return Subscribe.class;
    }
}
