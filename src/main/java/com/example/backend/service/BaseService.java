package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.IId;
import com.example.backend.mapper.IBaseMapper;
import com.example.backend.util.IValidates;
import com.example.backend.util.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class BaseService<M extends IBaseMapper<T>, T extends IId> extends ServiceImpl<M, T> implements IValidates {

    protected RedisHelper redisHelper;
    protected ApplicationEventPublisher eventPublisher;
    protected ObjectMapper objectMapper;

    // --- page

    /**
     * 转换 page 查询结果，生成最终 PageDTO
     */
    public <V, R> Page<R> convertDto(Page<V> result, Function<V, R> converter) {
        Page<R> response = PageDTO.of(result.getCurrent(), result.getSize(), result.getTotal());
        List<V> records = result.getRecords();
        response.setRecords(records.stream()
                .map(converter)
                .toList());
        return response;
    }

    // --- mapper

    /**
     * 根据 id 查找列
     */
    @SafeVarargs
    public final T selectById(Long id, SFunction<T, ?>... columns) {
        if (id == null) return null;
        return baseMapper.selectById(id, columns);
    }

    /**
     * 根据 id 查找完整对象，id 必须存在
     */
    public T requireById(Long id) {
        return baseMapper.requireById(id);
    }

    /**
     * 根据 id 查找列，id 必须存在
     */
    @SafeVarargs
    public final T requireById(Long id, SFunction<T, ?>... columns) {
        return baseMapper.requireById(id, columns);
    }

    /**
     * 检查 id 必须存在
     */
    public void requireExist(Long id) {
        baseMapper.requireExist(id);
    }

    /**
     * 根据条件获取唯一对象，对象必须存在
     */
    public T requireOne(Wrapper<T> queryWrapper) {
        return baseMapper.requireOne(queryWrapper);
    }

    /**
     * 根据给定 id 组获取指定列
     */
    @SafeVarargs
    public final List<T> listById(Set<Long> ids, SFunction<T, ?>... columns) {
        return baseMapper.selectList(ids, columns);
    }


    /**
     * 根据给定 id 组获取指定列，并以 id 为键创建 Map
     */
    @SafeVarargs
    public final Map<Long, T> groupById(Set<Long> ids, SFunction<T, ?>... columns) {
        return baseMapper.groupById(ids, columns);
    }

    // ---

    @Autowired
    public void setObjects(RedisHelper redisHelper,
                           ApplicationEventPublisher eventPublisher,
                           ObjectMapper objectMapper) {
        this.redisHelper = redisHelper;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }
}
