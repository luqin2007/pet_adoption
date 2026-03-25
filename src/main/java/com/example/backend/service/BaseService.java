package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.IId;
import com.example.backend.entity.User;
import com.example.backend.mapper.IBaseMapper;
import com.example.backend.util.CustomUserDetails;
import com.example.backend.util.RedisHelper;
import com.example.backend.util.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.function.Function;

public class BaseService<M extends IBaseMapper<T>, T extends IId> extends ServiceImpl<M, T> {

    protected RedisHelper redisHelper;
    protected ApplicationEventPublisher eventPublisher;

    // 权限校验

    /**
     * 获取当前登录用户
     */
    public static User getLoginUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> (CustomUserDetails) auth.getPrincipal())
                .map(CustomUserDetails::getUser)
                .orElseThrow(() -> ServiceException.auth("请先登录"));
    }

    /**
     * 权限校验
     */
    protected void requirePermission(boolean permission) {
        if (!permission) throw ServiceException.auth("权限不足");
    }

    /**
     * 确保对象存在
     */
    protected void requireExist(Object obj, String message) {
        if (ObjectUtils.isEmpty(obj)) throw ServiceException.notFound(message);
    }

    /**
     * 确保对象相同
     */
    protected void requireEqual(Object obj1, Object obj2, String message) {
        if (obj1 != null && !obj1.equals(obj2)) {
            throw ServiceException.invalidate(message);
        }
    }

    /**
     * 确保对象不同
     */
    protected void requireNotEqual(Object obj1, Object obj2, String message) {
        if (!Objects.equals(obj1, obj2)) {
            throw ServiceException.invalidate(message);
        }
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
