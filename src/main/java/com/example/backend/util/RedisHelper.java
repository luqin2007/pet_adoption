package com.example.backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class RedisHelper {

    private final RedisTemplate<String, Object> redisTemplateObject;
    private final RedisTemplate<String, String> redisTemplateString;

    public Long increment(String keyTemplate, Object... params) {
        String key = String.format(keyTemplate, params);
        return redisTemplateString.opsForValue().increment(key);
    }

    public void requireString(String key, String message) {
        if (!redisTemplateString.hasKey(key))
            throw ServiceException.invalidate(message);
    }

    public void putString(String key, String value) {
        redisTemplateString.opsForValue().set(key, value);
    }

    public void putString(String key, String value, long timeoutMinutes) {
        redisTemplateString.opsForValue().set(key, value, Duration.ofMinutes(timeoutMinutes));
    }

    public boolean hasString(String keyTemplate, Object... params) {
        return redisTemplateString.hasKey(String.format(keyTemplate, params));
    }

    public String getString(String key) {
        return redisTemplateString.opsForValue().get(key);
    }

    public String getAndDeleteString(String keyTemplate, Object... params) {
        String key = String.format(keyTemplate, params);
        return redisTemplateString.opsForValue().getAndDelete(key);
    }

    public void deleteString(String... keys) {
        if (keys.length == 1)
            redisTemplateString.delete(keys[0]);
        else if (keys.length > 1)
            redisTemplateString.delete(Set.of(keys));
    }

    public void expireString(String key, long timeoutMinutes) {
        redisTemplateString.expire(key, Duration.ofMinutes(timeoutMinutes));
    }

    public void putObjectToHash(String key, String hashKey, Object value) {
        redisTemplateObject.opsForHash().put(key, hashKey, value);
    }

    public void deleteObject(String... keys) {
        if (keys.length == 1)
            redisTemplateObject.delete(keys[0]);
        else if (keys.length > 1)
            redisTemplateObject.delete(Set.of(keys));
    }

    public <T> Stream<T> getObjectsFromHash(String key, Class<T> type) {
        return redisTemplateObject.opsForHash().values(key).stream().map(type::cast);
    }

    public void deleteObjectFromHash(String key, String hashKey) {
        redisTemplateObject.opsForHash().delete(key, hashKey);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getAndDeleteObjectsFromHash(String key, String hashKey) {
        return (List<T>) redisTemplateObject.opsForHash().getAndDelete(key, List.of(hashKey));
    }

    public void expireObject(String key, long timeoutMinutes) {
        redisTemplateObject.expire(key, Duration.ofMinutes(timeoutMinutes));
    }
}
