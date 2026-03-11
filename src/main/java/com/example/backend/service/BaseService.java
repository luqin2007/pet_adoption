package com.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.entity.IId;
import com.example.backend.mapper.IBaseMapper;
import com.example.backend.util.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class BaseService<M extends IBaseMapper<T>, T extends IId> extends ServiceImpl<M, T> {

    protected StringRedisTemplate redisTemplateString;
    protected RedisTemplate<String, Object> redisTemplateObject;
    protected ApplicationEventPublisher eventPublisher;

    // 权限校验

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
        if (obj1 != null && !obj1.equals(obj2))
            throw ServiceException.invalidate(message);
    }

    /**
     * 确保对象不同
     */
    protected void requireNotEqual(Object obj1, Object obj2, String message) {
        if (!Objects.equals(obj1, obj2))
            throw ServiceException.invalidate(message);
    }

    /**
     * 校验 Redis 键是否存在
     */
    protected void requireRedisString(String key, String message) {
        if (!redisTemplateString.hasKey(key))
            throw ServiceException.invalidate(message);
    }

    /**
     * 确保数据存在
     */
    protected T requireById(Long id, String message) {
        return getBaseMapper().requireById(id, message);
    }

    /**
     * 确保数据存在
     */
    protected <V> T requireOne(SFunction<T, V> field, V value, String message) {
        return getBaseMapper().requireOne(field, value, message);
    }

    /**
     * 确保数据存在
     */
    protected T requireOne(Wrapper<T> queryWrapper, String message) {
        return getBaseMapper().requireOne(queryWrapper, message);
    }

    // Redis

    /**
     * 向 Redis 中添加字符串
     */
    protected void putToRedis(String key, String value) {
        redisTemplateString.opsForValue().set(key, value);
    }

    /**
     * 向 Redis 中添加字符串
     *
     * @param timeoutMinutes 超时时间（分钟）
     */
    protected void putToRedis(String key, String value, long timeoutMinutes) {
        redisTemplateString.opsForValue().set(key, value, Duration.ofMinutes(timeoutMinutes));
    }

    /**
     * 从 Redis 中获取字符串
     */
    protected String getStringFromRedis(String key) {
        return redisTemplateString.opsForValue().get(key);
    }

    /**
     * 从 Redis 中删除字符串
     */
    protected void deleteStringFromRedis(String... keys) {
        if (keys.length == 1)
            redisTemplateString.delete(keys[0]);
        else if (keys.length > 1)
            redisTemplateString.delete(List.of(keys));
    }

    /**
     * 从 Redis 中获取并删除字符串
     */
    protected String getAndDeleteStringFromRedis(String key) {
        return redisTemplateString.opsForValue().getAndDelete(key);
    }

    /**
     * 从 Redis 中获取并 +1
     */
    protected Long incrementFromRedis(String key) {
        return redisTemplateObject.opsForValue().increment(key);
    }

    /**
     * 向 Redis 的 Hash 中添加对象
     */
    protected void putToRedisHash(String key, String hashKey, Object value) {
        redisTemplateObject.opsForHash().put(key, hashKey, value);
    }

    /**
     * 从 Redis 的 Hash 中获取并删除对象
     */
    protected <V> List<V> getAndDeleteFromRedisHash(String key, String hashKey) {
        return (List<V>) redisTemplateObject.opsForHash().getAndDelete(key, Set.of(hashKey));
    }

    /**
     * 从 Redis 的 Hash 中获取所有对象
     *
     * @param type 仅用于确认数据类型
     */
    protected <V> List<V> getAllFromRedisHash(String key, Class<V> type) {
        return (List<V>) redisTemplateObject.opsForHash().values(key);
    }

    /**
     * 从 Redis 中删除对象
     */
    protected void deleteObjectFromRedis(String... keys) {
        if (keys.length == 1)
            redisTemplateObject.delete(keys[0]);
        else if (keys.length > 1)
            redisTemplateObject.delete(List.of(keys));
    }

    @Autowired
    public void setObjects(StringRedisTemplate redisTemplateString,
                           RedisTemplate<String, Object> redisTemplateObject,
                           ApplicationEventPublisher eventPublisher) {
        this.redisTemplateString = redisTemplateString;
        this.redisTemplateObject = redisTemplateObject;
        this.eventPublisher = eventPublisher;
    }
}
