package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 文章类型
 */
public enum ArticleType {
    STORY, // 救助故事
    ACTIVITY, // 活动推广
    KNOWLEDGE; // 科普知识

    /**
     * 根据名称获取文章类型
     */
    public static ArticleType get(String name) {
        try {
            return ArticleType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("exception.invalidate.article_type");
        }
    }
}
