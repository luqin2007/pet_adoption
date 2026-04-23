package com.example.backend.dto;

import com.example.backend.entity.Article;
import com.example.backend.entity.property.ArticleStatus;
import com.example.backend.entity.property.ArticleType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

/**
 * 创建文章请求
 */
@Data
public class ArticleAddRequest implements IRequest, IValidatedRequest {

    /**
     * 文章类型
     */
    @NotBlank(message = "request.article.type")
    private String type;

    /**
     * 标题
     */
    @NotBlank(message = "request.article.title")
    private String title;

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
     * 是否直接发布
     */
    private Boolean publish;

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, ArticleAddRequest::getType, ArticleStatus.class, "request.article.type");
    }

    public Article create(Long authorId) {
        Date now = new Date();
        boolean published = Boolean.TRUE.equals(publish);
        return new Article(null,
                ArticleType.get(type),
                published ? ArticleStatus.PUBLISHED : ArticleStatus.DRAFT,
                title,
                content,
                cover,
                authorId,
                Boolean.FALSE,
                0,
                0,
                0,
                published ? now : null,
                now,
                now);
    }
}
