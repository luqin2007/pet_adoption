package com.example.backend.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.PageParams;
import com.example.backend.entity.IId;
import com.example.backend.mapper.IBaseMapper;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MPLambdaQuery<T extends IId> {

    private final LambdaQueryWrapper<T> query;
    private final IBaseMapper<T> mapper;

    public MPLambdaQuery(IBaseMapper<T> mapper) {
        this.query = Wrappers.lambdaQuery(mapper.getEntityClass());
        this.mapper = mapper;
    }

    public <V> MPLambdaQuery<T> in(SFunction<T, V> column, Collection<V> values) {
        if (ObjectUtils.isEmpty(values)) return this;

        // 去重
        Set<?> set = values instanceof Set
                ? (Set<?>) values
                : new HashSet<>(values);
        query
                .eq(set.size() == 1, column, set.iterator().next())
                .in(set.size() > 1, column, set);
        return this;
    }

    public <V> MPLambdaQuery<T> in(boolean condition, SFunction<T, V> column, Collection<V> values) {
        if (!condition) return this;
        return in(column, values);
    }

    public <V> MPLambdaQuery<T> in(SFunction<T, ?> column, Function<V, ?> converter, Collection<V> values) {
        if (values == null) return this;

        Set<?> set = values.stream()
                .map(converter)
                .collect(Collectors.toSet());
        query
                .eq(set.size() == 1, column, set.iterator().next())
                .in(set.size() > 1, column, set);
        return this;
    }

    public MPLambdaQuery<T> in(SFunction<T, Date> column, Date time0, Date time1) {
        query
                .ge(time0 != null, column, time0)
                .le(time1 != null, column, time1);
        return this;
    }

    public MPLambdaQuery<T> in(SFunction<T, BigDecimal> column, String value0, String value1) {
        if (value0 != null)
            query.ge(column, new BigDecimal(value0));
        if (value1 != null)
            query.le(column, new BigDecimal(value1));
        return this;
    }

    public <N extends Number> MPLambdaQuery<T> in(SFunction<T, N> column, N value0, N value1) {
        query
                .ge(value0 != null, column, value0)
                .le(value1 != null, column, value1);
        return this;
    }

    public <V> MPLambdaQuery<T> notIn(SFunction<T, ?> column, Collection<V> values) {
        if (values == null) return this;

        // 去重
        Set<?> set = values instanceof Set
                ? (Set<?>) values
                : new HashSet<>(values);
        query
                .ne(set.size() == 1, column, set.iterator().next())
                .notIn(set.size() > 1, column, set);
        return this;
    }

    public MPLambdaQuery<T> exist(SFunction<T, ?> column, Boolean isExist) {
        query
                .isNotNull(Boolean.TRUE.equals(isExist), column)
                .isNull(Boolean.FALSE.equals(isExist), column);
        return this;
    }

    @SafeVarargs
    public final MPLambdaQuery<T> like(SFunction<T, ?> column, String text, SFunction<T, ?>... otherColumns) {
        query.like(!ObjectUtils.isEmpty(text), column, text);
        for (SFunction<T, ?> c : otherColumns) {
            query.or();
            query.like(!ObjectUtils.isEmpty(text), c, text);
        }
        return this;
    }

    public <V> MPLambdaQuery<T> eq(SFunction<T, V> column, V value) {
        query.eq(!ObjectUtils.isEmpty(value), column, value);
        return this;
    }

    public <V> MPLambdaQuery<T> eq(boolean condition, SFunction<T, V> column, V value) {
        query.eq(condition, column, value);
        return this;
    }

    public <V, R> MPLambdaQuery<T> eq(SFunction<T, R> column, Function<V, R> converter, V value) {
        query.eq(!ObjectUtils.isEmpty(value), column, converter.apply(value));
        return this;
    }

    public <V> MPLambdaQuery<T> ne(SFunction<T, V> column, V value) {
        query.ne(column, value);
        return this;
    }

    public <V> MPLambdaQuery<T> desc(SFunction<T, V> column) {
        query.orderByDesc(column);
        return this;
    }

    public <V> MPLambdaQuery<T> asc(SFunction<T, V> column) {
        query.orderByAsc(column);
        return this;
    }

    public MPLambdaQuery<T> limit(Integer count) {
        query.last(count != null, "LIMIT " + count);
        return this;
    }

    public MPLambdaQuery<T> apply(String sql, Object... params) {
        query.apply(sql, params);
        return this;
    }

    @SafeVarargs
    public final MPLambdaQuery<T> select(SFunction<T, ?>... columns) {
        query.select(columns);
        return this;
    }

    // --------------------------

    public Page<T> page(PageParams pageRequest) {
        Page<T> page = pageRequest.createPage();
        return mapper.selectPage(page, query);
    }

    public <K> Map<K, T> group(Function<T, K> keyMapper) {
        Map<K, T> map = new HashMap<>();
        for (T t : mapper.selectList(query)) {
            map.putIfAbsent(keyMapper.apply(t), t);
        }
        return map;
    }

    @SafeVarargs
    public final <K> Map<K, T> group(Function<T, K> keyMapper, SFunction<T, ?>... columns) {
        Map<K, T> map = new HashMap<>();
        for (T t : mapper.selectList(query, columns)) {
            map.putIfAbsent(keyMapper.apply(t), t);
        }
        return map;
    }

    public Map<Long, T> groupById() {
        return mapper.group(query);
    }

    @SafeVarargs
    public final Map<Long, T> groupById(SFunction<T, ?>... columns) {
        query.select(columns);
        return mapper.group(query);
    }

    public Map<Long, List<T>> groupList(SFunction<T, Long> keyMapper) {
        return mapper.groupList(query, keyMapper, Function.identity());
    }

    public <R> Map<Long, List<R>> groupList(SFunction<T, Long> keyMapper, Function<T, R> converter) {
        return mapper.groupList(query, keyMapper, converter);
    }

    public boolean exists() {
        return mapper.exists(query);
    }

    public List<T> list() {
        return mapper.selectList(query);
    }

    public <R> Stream<R> list(SFunction<T, R> column) {
        //noinspection unchecked
        return mapper.selectList(query, column).stream().map(column);
    }

    @SafeVarargs
    public final List<T> list(SFunction<T, ?>... columns) {
        return mapper.selectList(query, columns);
    }

    public T one() {
        return mapper.selectOne(query, false);
    }

    @SafeVarargs
    public final T one(SFunction<T, ?>... columns) {
        query.select(columns);
        return mapper.selectOne(query, false);
    }

    public Optional<T> opt() {
        return Optional.ofNullable(mapper.selectOne(query, false));
    }

    public T require() {
        return mapper.requireOne(query);
    }

    public void requireExist() {
        mapper.requireExist(query);
    }

    public void delete() {
        mapper.delete(query);
    }

    public Long count() {
        return mapper.selectCount(query);
    }
}
