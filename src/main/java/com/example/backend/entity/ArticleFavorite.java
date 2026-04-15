package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 文章收藏记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleFavorite implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 文章 id
     * *外键:article(id) 非空 bigint*
     */
    private Long articleId;

    /**
     * 用户 id
     * *外键:user(id) 非空 bigint*
     */
    private Long userId;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;
}
