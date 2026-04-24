package com.example.backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

/**
 * Redis 工具类
 */
@Component
@RequiredArgsConstructor
public class RedisHelper {

    private final RedisTemplate<Object, Object> redisTemplateObject;
    private final StringRedisTemplate redisTemplateString;

    /**
     * 存储字符串
     *
     * @param key            键
     * @param value          值
     * @param timeoutMinutes 超时时间，单位：分钟
     */
    public void putString(String key, String value, long timeoutMinutes) {
        redisTemplateString.opsForValue().set(key, value, Duration.ofMinutes(timeoutMinutes));
    }

    /**
     * 存储字符串
     *
     * @param key            键
     * @param value          值
     * @param timeoutSeconds 超时时间，单位：秒
     */
    public void putStringSec(String key, String value, long timeoutSeconds) {
        redisTemplateString.opsForValue().set(key, value, Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * 检查字符串键 key 是否存在，使用 <code>String.format(keyTemplate, params)</code> 生成键
     *
     * @param keyTemplate 键模板
     * @param params      参数
     */
    public boolean hasString(String keyTemplate, Object... params) {
        return redisTemplateString.hasKey(String.format(keyTemplate, params));
    }

    /**
     * 获取字符串
     *
     * @param key 键
     */
    public String getString(String key) {
        return redisTemplateString.opsForValue().get(key);
    }

    /**
     * 获取字符串并删除，使用 <code>String.format(keyTemplate, params)</code> 生成键
     *
     * @param keyTemplate 键模板
     * @param params      参数
     */
    public String getAndDeleteString(String keyTemplate, Object... params) {
        String key = String.format(keyTemplate, params);
        return redisTemplateString.opsForValue().getAndDelete(key);
    }

    /**
     * 删除字符串
     */
    public void deleteString(String key) {
        redisTemplateString.delete(key);
    }

    /**
     * 设置/重置字符串超时时间
     *
     * @param key            键
     * @param timeoutMinutes 超时时间，单位：分钟
     */
    public void expireString(String key, long timeoutMinutes) {
        redisTemplateString.expire(key, Duration.ofMinutes(timeoutMinutes));
    }

    /**
     * 向 Hash 存储对象
     *
     * @param key     Hash 键
     * @param hashKey Hash 内对象键
     * @param value   值
     */
    public void putObjectToHash(String key, String hashKey, Object value) {
        redisTemplateObject.opsForHash().put(key, hashKey, value);
    }

    /**
     * 检查字符串键 key 是否存在，使用 <code>String.format(keyTemplate, params)</code> 生成键
     *
     * @param keyTemplate 键模板
     * @param params      参数
     */
    public boolean hasObject(String keyTemplate, Object... params) {
        return redisTemplateObject.hasKey(String.format(keyTemplate, params));
    }

    /**
     * 删除对象
     *
     * @param key 键
     */
    public void deleteObject(String key) {
        redisTemplateObject.delete(key);
    }

    /**
     * 获取 Hash 中的所有对象
     *
     * @param key  Hash 键
     * @param type 对象类型，用于类型转换
     */
    public <T> Stream<T> getObjectsFromHash(String key, Class<T> type) {
        return redisTemplateObject.opsForHash().values(key).stream().map(type::cast);
    }

    /**
     * 获取 Hash 中的对象并删除
     *
     * @param key     Hash 键
     * @param hashKey Hash 内对象键
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getAndDeleteObjectsFromHash(String key, String hashKey) {
        Object value = redisTemplateObject.opsForHash().get(key, hashKey);
        if (value == null) {
            return List.of();
        }
        redisTemplateObject.opsForHash().delete(key, hashKey);
        return List.of((T) value);
    }

    /**
     * 设置/重置对象超时时间
     *
     * @param key            键
     * @param timeoutMinutes 超时时间，单位：分钟
     */
    public void expireObject(String key, long timeoutMinutes) {
        redisTemplateObject.expire(key, Duration.ofMinutes(timeoutMinutes));
    }
}
