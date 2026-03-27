package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.example.backend.entity.IId;
import com.example.backend.util.IBaseCheck;
import com.example.backend.util.ServiceException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface IBaseMapper<T extends IId> extends BaseMapper<T>, IBaseCheck {

    default LambdaQueryWrapper<T> lambdaQuery() {
        return Wrappers.lambdaQuery();
    }

    default LambdaUpdateWrapper<T> lambdaUpdate() {
        return Wrappers.lambdaUpdate();
    }

    @SuppressWarnings("unchecked")
    default T selectById(Long id, SFunction<T, ?>... columns) {
        return selectOne(lambdaQuery().eq(T::getId, id).select(columns));
    }

    default <V> void updateById(Long id, SFunction<T, V> column, V value) {
        update(lambdaUpdate().set(column, value).eq(T::getId, id));
    }

    default T requireById(Long id) {
        return requireExist(selectById(id));
    }

    @SuppressWarnings("unchecked")
    default T requireById(Long id, SFunction<T, ?>... columns) {
        return requireOne(lambdaQuery().eq(T::getId, id).select(columns));
    }

    default void requireExist(Long id) {
        if (!exists(lambdaQuery().eq(T::getId, id)))
            throw ServiceException.notFound(getMissingMessage());
    }

    default void requireExist(Wrapper<T> queryWrapper) {
        if (!exists(queryWrapper))
            throw ServiceException.notFound(getMissingMessage());
    }

    default <V> T requireOne(SFunction<T, V> field, V value) {
        Wrapper<T> wrapper = Wrappers.<T>lambdaQuery().eq(field, value);
        return requireExist(selectOne(wrapper));
    }

    default T requireOne(Wrapper<T> queryWrapper) {
        return requireExist(selectOne(queryWrapper));
    }

    default List<T> selectList(Set<Long> ids) {
        return selectList(lambdaQuery().in(T::getId, ids));
    }

    @SuppressWarnings("unchecked")
    default List<T> selectList(Set<Long> ids, SFunction<T, ?>... columns) {
        return selectList(lambdaQuery().in(T::getId, ids).select(columns));
    }

    @SuppressWarnings("unchecked")
    default <V> List<T> selectList(LambdaQueryWrapper<T> wrapper, SFunction<T, ?>... columns) {
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
        return selectList(ids, columns).stream().collect(Collectors.toMap(T::getId, Function.identity()));
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

    private T requireExist(T obj) {
        if (obj == null) throw ServiceException.notFound(getMissingMessage());
        return obj;
    }

    default String getMissingMessage() {
        return "信息不存在";
    }
}
