package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.example.backend.entity.IId;
import com.example.backend.util.*;
import com.github.yulichang.base.MPJBaseMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface IBaseMapper<T extends IId> extends MPJBaseMapper<T>, IValidates {

    default MPLambdaQuery<T> lambdaQuery() {
        return new MPLambdaQuery<>(this);
    }

    default MPLambdaUpdate<T> lambdaUpdate() {
        return new MPLambdaUpdate<>(this);
    }

    default <DTO> MPJLambdaQuery<T, T, DTO> joinQuery() {
        return new MPJLambdaQuery<>(this);
    }

    @SuppressWarnings("unchecked")
    default T selectById(Long id, SFunction<T, ?>... columns) {
        return selectOne(Wrappers.lambdaQuery(getEntityClass()).eq(T::getId, id).select(columns));
    }

    default T requireById(Long id) {
        return requireExist(selectById(id));
    }

    @SuppressWarnings("unchecked")
    default T requireById(Long id, SFunction<T, ?>... columns) {
        return requireOne(Wrappers.lambdaQuery(getEntityClass()).eq(T::getId, id).select(columns));
    }

    default void requireExist(Long id) {
        if (!exists(Wrappers.lambdaQuery(getEntityClass()).eq(T::getId, id)))
            throw ServiceException.notFound(getMissingMessage());
    }

    default void requireExist(Wrapper<T> queryWrapper) {
        if (!exists(queryWrapper))
            throw ServiceException.notFound(getMissingMessage());
    }

    default T requireOne(Wrapper<T> queryWrapper) {
        return requireExist(selectOne(queryWrapper));
    }

    default List<T> selectList(Set<Long> ids) {
        if (ids.isEmpty()) return List.of();
        return selectList(Wrappers.lambdaQuery(getEntityClass()).in(T::getId, ids));
    }

    @SuppressWarnings("unchecked")
    default List<T> selectList(Set<Long> ids, SFunction<T, ?>... columns) {
        if (ids.isEmpty()) return List.of();
        return selectList(Wrappers.lambdaQuery(getEntityClass()).in(T::getId, ids).select(columns));
    }

    @SuppressWarnings("unchecked")
    default List<T> selectList(LambdaQueryWrapper<T> wrapper, SFunction<T, ?>... columns) {
        return selectList(wrapper.select(columns));
    }

    default <R> Map<Long, R> group(Wrapper<T> wrapper, Function<T, R> converter) {
        return selectList(wrapper).stream().collect(Collectors.toMap(T::getId, converter));
    }

    default Map<Long, T> group(Wrapper<T> wrapper) {
        return group(wrapper, Function.identity());
    }

    default Map<Long, T> groupById(Set<Long> ids) {
        return selectList(ids).stream().collect(Collectors.toMap(T::getId, Function.identity()));
    }

    @SuppressWarnings("unchecked")
    default Map<Long, T> groupById(Set<Long> ids, SFunction<T, ?>... columns) {
        if (ids.isEmpty()) return Map.of();
        return selectList(ids, columns).stream().collect(Collectors.toMap(T::getId, Function.identity()));
    }

    @SuppressWarnings("unchecked")
    default Map<Long, T> groupById(Stream<Long> ids, SFunction<T, ?>... columns) {
        return groupById(ids.filter(Objects::nonNull).collect(Collectors.toSet()), columns);
    }

    @SuppressWarnings("unchecked")
    default Map<Long, T> groupById(Stream<Long> ids1, Stream<Long> ids2, SFunction<T, ?>... columns) {
        return groupById(Stream.concat(ids1, ids2).filter(Objects::nonNull).collect(Collectors.toSet()), columns);
    }

    default <R> Map<Long, List<R>> groupList(Wrapper<T> wrapper, SFunction<T, Long> keyColumn, Function<T, R> converter) {
        List<T> results = selectList(wrapper);
        Map<Long, List<R>> map = new HashMap<>();
        for (T result : results) {
            Long key = keyColumn.apply(result);
            R element = converter.apply(result);
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(element);
        }
        return map;
    }

    default <R> Map<Long, R> groupFirst(Wrapper<T> wrapper, SFunction<T, Long> keyColumn, Function<T, R> converter) {
        List<T> results = selectList(wrapper);
        Map<Long, R> map = new HashMap<>();
        for (T result : results) {
            Long key = keyColumn.apply(result);
            if (!map.containsKey(key))
                map.put(key, converter.apply(result));
        }
        return map;
    }

    private T requireExist(T obj) {
        if (obj == null) throw ServiceException.notFound(getMissingMessage());
        return obj;
    }

    default String getMissingMessage() {
        return "exception.not_found.data";
    }

    Class<T> getEntityClass();
}

