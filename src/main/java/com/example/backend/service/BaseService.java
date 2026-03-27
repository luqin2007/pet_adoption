package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.IId;
import com.example.backend.entity.User;
import com.example.backend.mapper.IBaseMapper;
import com.example.backend.util.CustomUserDetails;
import com.example.backend.util.IBaseCheck;
import com.example.backend.util.RedisHelper;
import com.example.backend.util.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;

public class BaseService<M extends IBaseMapper<T>, T extends IId> extends ServiceImpl<M, T> implements IBaseCheck {

    protected RedisHelper redisHelper;
    protected ApplicationEventPublisher eventPublisher;

    // --- page

    public <V, R> Page<R> convertDto(Page<V> result, Function<V, R> converter) {
        Page<R> response = PageDTO.of(result.getCurrent(), result.getSize(), result.getTotal());
        List<V> records = result.getRecords();
        response.setRecords(records.stream()
                .map(converter)
                .toList());
        return response;
    }

    // --- mapper

    @SafeVarargs
    public final T selectById(Long id, SFunction<T, ?>... columns) {
        return baseMapper.selectById(id, columns);
    }

    public <V> void updateById(Long id, SFunction<T, V> column, V value) {
        baseMapper.updateById(id, column, value);
    }

    public T requireById(Long id) {
        return baseMapper.requireById(id);
    }

    @SafeVarargs
    public final T requireById(Long id, SFunction<T, ?>... columns) {
        return baseMapper.requireById(id, columns);
    }

    public void requireExist(Long id) {
        baseMapper.requireExist(id);
    }

    public void requireExist(Wrapper<T> queryWrapper) {
        baseMapper.requireExist(queryWrapper);
    }

    public <V> T requireOne(SFunction<T, V> field, V value) {
        return baseMapper.requireOne(field, value);
    }

    public T requireOne(Wrapper<T> queryWrapper) {
        return baseMapper.requireOne(queryWrapper);
    }

    public List<T> listById(Set<Long> ids) {
        return baseMapper.selectList(ids);
    }

    @SafeVarargs
    public final List<T> listById(Set<Long> ids, SFunction<T, ?>... columns) {
        return baseMapper.selectList(ids, columns);
    }

    @SafeVarargs
    public final <V> List<T> list(LambdaQueryWrapper<T> wrapper, SFunction<T, ?>... columns) {
        return baseMapper.selectList(wrapper, columns);
    }

    public <R> Map<Long, R> group(Wrapper<T> wrapper, Function<T, R> converter) {
        return baseMapper.group(wrapper, converter);
    }

    public Map<Long, T> group(Wrapper<T> wrapper) {
        return baseMapper.group(wrapper);
    }

    public Map<Long, T> groupById(Set<Long> ids) {
        return baseMapper.groupById(ids);
    }

    @SafeVarargs
    public final Map<Long, T> groupById(Set<Long> ids, SFunction<T, ?>... columns) {
        return baseMapper.groupById(ids, columns);
    }

    public <R> Map<Long, List<R>> groupList(Wrapper<T> wrapper, SFunction<T, Long> keyColumn, Function<T, R> converter) {
        return baseMapper.groupList(wrapper, keyColumn, converter);
    }

    // ---

    @Autowired
    public void setObjects(RedisHelper redisHelper, ApplicationEventPublisher eventPublisher) {
        this.redisHelper = redisHelper;
        this.eventPublisher = eventPublisher;
    }
}
