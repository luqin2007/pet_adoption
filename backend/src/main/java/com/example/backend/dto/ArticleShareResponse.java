package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 文章分享信息
 */
@Data
@AllArgsConstructor
public class ArticleShareResponse implements IResponse {

    /**
     * 文章 id
     */
    private Long articleId;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 详情页地址
     */
    private String detailUrl;
}
