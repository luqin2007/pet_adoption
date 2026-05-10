package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 文章状态
 */
public enum ArticleStatus {
    DRAFT, // 草稿
    PUBLISHED, // 已发布
    OFFLINE; // (仅活动、招募) 已下线

    public boolean canChangeFrom(ArticleStatus status) {
        return switch (this) {
            case PUBLISHED -> status == DRAFT;
            case OFFLINE, DRAFT -> status == PUBLISHED;
        };
    }

    /**
     * 根据名称获取文章状态
     */
    public static ArticleStatus get(String name) {
        try {
            return ArticleStatus.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw ServiceException.invalidate("exception.invalidate.article_status");
        }
    }
}
