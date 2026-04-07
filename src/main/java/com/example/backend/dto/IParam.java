package com.example.backend.dto;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface IParam<T> extends IRequest {

    default IParam<T> querySet(LambdaQueryWrapper<T> query, SFunction<T, ?> column, Collection<?> values) {
        if (values == null) return self();

        // 去重
        Set<?> set = values instanceof Set
                ? (Set<?>) values
                : Set.of(values);
        query
                .eq(set.size() == 1, column, set.iterator().next())
                .in(set.size() != 1, column, set);
        return self();
    }

    default <V> IParam<T> querySet(LambdaQueryWrapper<T> query, SFunction<T, ?> column, Function<V, ?> converter, Collection<V> values) {
        if (values == null) return self();

        Set<?> set = values.stream()
                .map(converter)
                .collect(Collectors.toSet());
        query
                .eq(set.size() == 1, column, set.iterator().next())
                .in(set.size() != 1, column, set);
        return self();
    }

    default IParam<T> queryTime(LambdaQueryWrapper<T> query, SFunction<T, Date> column, Date time0, Date time1) {
        query
                .ge(time0 != null, column, time0)
                .le(time1 != null, column, time1);
        return self();
    }

    default IParam<T> queryDecimal(LambdaQueryWrapper<T> query, SFunction<T, BigDecimal> column, String value0, String value1) {
        if (value0 != null)
            query.ge(column, new BigDecimal(value0));
        if (value1 != null)
            query.le(column, new BigDecimal(value1));
        return self();
    }

    default IParam<T> queryExist(LambdaQueryWrapper<T> query, SFunction<T, ?> column, Boolean isExist) {
        query.isNotNull(Boolean.TRUE.equals(isExist), column);
        query.isNull(Boolean.FALSE.equals(isExist), column);
        return self();
    }

    @SuppressWarnings("unchecked")
    default IParam<T> queryText(LambdaQueryWrapper<T> query, SFunction<T, ?> column, String text, SFunction<T, ?>... otherColumns) {
        query.like(text != null, column, text);
        for (SFunction<T, ?> c : otherColumns) {
            query.or().like(text != null, c, text);
        }
        return self();
    }

    default <V> IParam<T> query(LambdaQueryWrapper<T> query, SFunction<T, V> column, V value) {
        query.eq(!ObjectUtils.isEmpty(value), column, value);
        return self();
    }

    default <V, R> IParam<T> query(LambdaQueryWrapper<T> query, SFunction<T, R> column, Function<V, R> converter, V value) {
        query.eq(!ObjectUtils.isEmpty(value), column, converter.apply(value));
        return self();
    }
}
