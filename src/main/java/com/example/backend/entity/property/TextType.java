package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

/**
 * 文本类型
 */
public enum TextType {
    NONE,      // 无
    PLAIN,     // 纯文本
    JSON,      // Json
    MARKDOWN;  // Markdown

    public static TextType get(String name) {
        try {
            return TextType.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.text_type");
        }
    }
}