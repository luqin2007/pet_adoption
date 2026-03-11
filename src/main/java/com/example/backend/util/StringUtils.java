package com.example.backend.util;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

public class StringUtils {

    private static final Random random = new Random(System.currentTimeMillis());

    /**
     * 生成随机字符串，包含 大小写字母、数字
     * @param length 字符串长度
     */
    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(3);
            switch (number) {
                case 0:
                    sb.append((char) Math.round(Math.random() * 25 + 65));
                    break;
                case 1:
                    sb.append((char) Math.round(Math.random() * 25 + 97));
                    break;
                case 2:
                    sb.append(Math.round(Math.random() * 9));
                    break;
            }
        }
        return sb.toString();
    }

    public static String randomUUID(String keyTemplate, StringRedisTemplate redisTemplate, int maxTimes) {
        String randomId, redisKey;
        int retryTimes = 0;
        do {
            randomId = UUID.randomUUID().toString();
            redisKey = String.format(keyTemplate, randomId);
            if (retryTimes++ > maxTimes)
                throw ServiceException.unavailable("请稍后重试");
        } while (redisTemplate.hasKey(redisKey));
        return randomId;
    }

    /**
     * Spring StringUtils.hasText 的别名，避免引入 StringUtils
     *
     * @see org.springframework.util.StringUtils#hasText(String)
     */
    public static boolean hasText(String str) {
        return org.springframework.util.StringUtils.hasText(str);
    }
}
