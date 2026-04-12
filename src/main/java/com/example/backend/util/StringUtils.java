package com.example.backend.util;

import tools.jackson.databind.ObjectMapper;

import java.util.Random;
import java.util.UUID;

/**
 * 字符串类工具
 */
public class StringUtils {

    private static final Random random = new Random(System.currentTimeMillis());

    /**
     * 生成随机字符串，包含 大小写字母、数字
     *
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

    /**
     * 随机生成不重复的 UUID
     *
     * @param keyTemplate 缓存 key 模板，使用 <code>String.format(keyTemplate, uuid)</code> 创建 key
     * @param redisHelper Redis 访问工具
     * @param maxTimes    最大重试次数
     * @throws ServiceException 重试次数耗尽后仍未生成不重复的 UUID
     */
    public static String randomUUID(String keyTemplate, RedisHelper redisHelper, int maxTimes) {
        String randomId;
        int retryTimes = 0;
        do {
            randomId = UUID.randomUUID().toString();
            if (retryTimes++ > maxTimes)
                throw ServiceException.unavailable("exception.unavailable.retry");
        } while (redisHelper.hasString(keyTemplate, randomId));
        return randomId;
    }

    /**
     * <code>org.springframework.util.StringUtils.hasText(str)</code> 的别名，避免引入 StringUtils
     *
     * @see org.springframework.util.StringUtils#hasText(String)
     */
    public static boolean hasText(String str) {
        return org.springframework.util.StringUtils.hasText(str);
    }
}
