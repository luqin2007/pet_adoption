package com.example.backend.util;

import java.util.Random;

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

    /**
     * Spring StringUtils.hasText 的别名，避免引入 StringUtils
     *
     * @see org.springframework.util.StringUtils#hasText(String)
     */
    public static boolean hasText(String str) {
        return org.springframework.util.StringUtils.hasText(str);
    }
}
