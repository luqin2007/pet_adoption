package com.example.backend.util;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.example.backend.entity.IId;
import com.example.backend.mapper.IBaseMapper;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.Set;

public class MPLambdaUpdate<T extends IId> {

    private final LambdaUpdateWrapper<T> query;
    private final IBaseMapper<T> mapper;

    public MPLambdaUpdate(IBaseMapper<T> mapper) {
        this.query = Wrappers.lambdaUpdate(mapper.getEntityClass());
        this.mapper = mapper;
    }

    public <V> MPLambdaUpdate<T> in(SFunction<T, V> column, Collection<V> values) {
        if (ObjectUtils.isEmpty(values)) return this;

        // 去重
        Set<?> set = values instanceof Set
                ? (Set<?>) values
                : Set.of(values);
        query
                .eq(set.size() == 1, column, set.iterator().next())
                .in(set.size() != 1, column, set);
        return this;
    }

    public <V> MPLambdaUpdate<T> eq(SFunction<T, V> column, V value) {
        query.eq(column, value);
        return this;
    }

    public <V> MPLambdaUpdate<T> set(SFunction<T, V> column, V value) {
        query.set(column, value);
        return this;
    }

    public <V> MPLambdaUpdate<T> set(boolean condition, SFunction<T, V> column, V value) {
        query.set(condition, column, value);
        return this;
    }

    public void update() {
        mapper.update(query);
    }
}
