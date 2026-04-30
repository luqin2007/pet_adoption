package com.example.backend.dto;

import com.example.backend.entity.Article;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ArticleStatus;
import com.example.backend.entity.property.ArticleType;
import com.example.backend.util.FileUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

import static com.example.backend.entity.property.ParentType.ARTICLE;
import static com.example.backend.entity.property.ParentType.USER;

/**
 * 文章响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleResponse implements IResponse {

    /**
     * 文章 id
     */
    private Long id;
    /**
     * 文章类型
     */
    private ArticleType type;
    /**
     * 文章状态
     */
    private ArticleStatus status;
    /**
     * 标题
     */
    private String title;
    /**
     * 正文
     */
    private String content;
    /**
     * 封面图
     */
    private String cover;
    /**
     * 作者 id
     */
    private Long authorId;
    /**
     * 作者名称
     */
    private String authorName;
    /**
     * 作者头像
     */
    private String authorAvatar;
    /**
     * 浏览量
     */
    private Integer viewCount;
    /**
     * 点赞数
     */
    private Integer likeCount;
    /**
     * 分享数
     */
    private Integer shareCount;
    /**
     * 当前用户是否已点赞
     */
    private Boolean liked;
    /**
     * 当前用户是否已收藏
     */
    private Boolean favorited;
    /**
     * 发布时间
     */
    private Date publishTime;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 根据实体构造响应
     */
    public static ArticleResponse create(Article article, User author, boolean clearContent) {
        return new ArticleResponse(
                article.getId(),
                article.getType(),
                article.getStatus(),
                article.getTitle(),
                clearContent ? "" : article.getContent(),
                FileUtils.generateAssetUrl(ARTICLE, article.getId(), article.getCover()),
                article.getAuthorId(),
                author == null ? null : author.getUsername(),
                author == null ? null : FileUtils.generateAssetUrl(USER, author.getId(), author.getAvatar()),
                article.getViewCount(),
                article.getLikeCount(),
                article.getShareCount(),
                Boolean.FALSE,
                Boolean.FALSE,
                article.getPublishTime(),
                article.getCreateTime(),
                article.getUpdateTime());
    }

    /**
     * 批量构造响应
     */
    public static ArticleResponse createBatch(Article article, Map<Long, User> users, boolean clearContent) {
        return create(article, users.get(article.getAuthorId()), clearContent);
    }
}
