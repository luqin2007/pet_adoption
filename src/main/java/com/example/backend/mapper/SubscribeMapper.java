package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.dto.SubscribeQueryParams;
import com.example.backend.entity.Subscribe;
import com.example.backend.entity.property.SubscribeAction;
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

    default LambdaQueryWrapper<Subscribe> queryByRequest(SubscribeQueryParams params) {
        LambdaQueryWrapper<Subscribe> query = lambdaQuery();
        params.querySet(query, Subscribe::getUserId, params.getUser())
                .querySet(query, Subscribe::getAction, SubscribeAction::get, params.getAction())
                .querySet(query, Subscribe::getElementId, params.getElement())
                .queryDecimal(query, Subscribe::getCount, params.getMin(), params.getMax())
                .queryTime(query, Subscribe::getCreateTime, params.getTime0(), params.getTime1());
        return query;
    }

    @Override
    default String getMissingMessage() {
        return "订阅不存在";
    }
}
