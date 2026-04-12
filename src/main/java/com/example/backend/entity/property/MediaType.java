package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 媒体类型
 */
public enum MediaType {
    IMAGE, // 图片
    VIDEO; // 视频

    public boolean isImage() {
        return this == IMAGE;
    }

    public static MediaType get(String name) {
        try {
            return MediaType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.media_type");
        }
    }
}
