package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.example.backend.entity.IId;
import com.example.backend.mapper.IBaseMapper;
import com.example.backend.util.IValidates;
import com.example.backend.util.RedisHelper;
import com.example.backend.util.ServiceException;
import com.example.backend.util.StringUtils;
import com.github.yulichang.base.MPJBaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public class BaseService<M extends IBaseMapper<T>, T extends IId> extends MPJBaseServiceImpl<M, T> implements IValidates {

    protected RedisHelper redisHelper;
    protected ApplicationEventPublisher eventPublisher;
    protected ObjectMapper objectMapper;
    protected MessageSource messageSource;
    protected TransactionTemplate transactionTemplate;

    @Value("${application.key_timeout}")
    protected long keyTimeout;

    // --- redis

    public String beginRedisUuid(String keyTemplate, String content) {
        String uuid = StringUtils.randomUUID(keyTemplate, redisHelper, 10);
        String redisKey = String.format(keyTemplate, uuid);
        redisHelper.putString(redisKey, content, keyTimeout);
        return uuid;
    }

    public String requireRedisUuid(String keyTemplate, String uuid) {
        String redisKey = String.format(keyTemplate, uuid);
        if (!redisHelper.hasString(keyTemplate, uuid))
            throw ServiceException.invalidate("exception.invalidate.request_timeout");
        redisHelper.expireString(redisKey, keyTimeout);
        return redisKey;
    }

    // message

    public String getMessage(String key, Object... params) {
        return messageSource.getMessage(key, params, LocaleContextHolder.getLocale());
    }

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

    /**
     * 根据给定 id 组获取指定列，并以 id 为键创建 Map
     */
    @SafeVarargs
    public final Map<Long, T> groupById(Stream<Long> ids, SFunction<T, ?>... columns) {
        return baseMapper.groupById(ids, columns);
    }

    /**
     * 根据给定 id 组获取指定列，并以 id 为键创建 Map
     */
    @SafeVarargs
    public final Map<Long, T> groupById(Stream<Long> ids1, Stream<Long> ids2, SFunction<T, ?>... columns) {
        return baseMapper.groupById(ids1, ids2, columns);
    }

    // ---

    @Autowired
    public void setObjects(RedisHelper redisHelper, // redis
                           ApplicationEventPublisher eventPublisher, // 事件
                           ObjectMapper objectMapper, // json
                           MessageSource messageSource, // i18n
                           TransactionTemplate transactionTemplate) { // transaction
        this.redisHelper = redisHelper;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
        this.transactionTemplate = transactionTemplate;
    }
}
