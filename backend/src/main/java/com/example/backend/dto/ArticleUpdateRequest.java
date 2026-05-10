package com.example.backend.dto;

import com.example.backend.entity.Article;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * 修改文章请求
 */
@Data
public class ArticleUpdateRequest implements IRequest {

    /**
     * 标题
     */
    @NotBlank(message = "request.article.title")
    private String title;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 正文
     */
    @NotBlank(message = "request.article.content")
    private String content;

    /**
     * 封面图路径
     */
    private String cover;

    /**
     * 标签
     */
    private String tags;

    /**
     * 将请求数据写入文章实体
     */
    public void applyTo(Article article) {
        article.setTitle(title);
        article.setContent(content);
        article.setCover(cover);
        article.setUpdateTime(new Date());
    }
}
