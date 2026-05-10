package com.example.backend.entity;

import com.example.backend.entity.property.ArticleStatus;
import com.example.backend.entity.property.ArticleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 宣传文章
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 文章类型
     * *非空 varchar(20)*
     */
    private ArticleType type;

    /**
     * 文章状态
     * *非空 varchar(20)*
     */
    private ArticleStatus status;

    /**
     * 标题
     * *非空 varchar(255)*
     */
    private String title;

    /**
     * 正文
     * *非空 text*
     */
    private String content;

    /**
     * 封面
     * *varchar(255)*
     */
    private String cover;

    /**
     * 作者 id
     * *外键:user(id) 非空 bigint*
     */
    private Long authorId;

    /**
     * 已弃用
     * *非空 boolean*
     */
    private Boolean isDiscard;

    /**
     * 浏览量
     * *非空 int*
     */
    private Integer viewCount;

    /**
     * 点赞数
     * *非空 int*
     */
    private Integer likeCount;

    /**
     * 分享数
     * *非空 int*
     */
    private Integer shareCount;

    /**
     * 发布时间
     * *datetime*
     */
    private Date publishTime;

    /**
     * 创建时间
     * *非空 datetime*
     */
    private Date createTime;

    /**
     * 修改时间
     * *非空 datetime*
     */
    private Date updateTime;

    public Integer getLikeCount() {
        return likeCount == null ? 0 : likeCount;
    }

    public Integer getViewCount() {
        return viewCount == null ? 0 : viewCount;
    }

    public Integer getShareCount() {
        return shareCount == null ? 0 : shareCount;
    }
}
