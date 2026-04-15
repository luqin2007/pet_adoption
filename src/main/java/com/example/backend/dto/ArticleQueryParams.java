package com.example.backend.dto;

import com.example.backend.entity.property.ArticleStatus;
import com.example.backend.entity.property.ArticleType;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

/**
 * 文章查询参数
 */
@Data
public class ArticleQueryParams implements IParam, IValidatedRequest {

    /**
     * 文章类型
     */
    private String type;

    /**
     * 作者 id
     */
    private Long author;

    /**
     * 文章状态
     */
    private Set<String> status;

    /**
     * 标题
     */
    private String title;

    /**
     * 标签
     */
    private String tag;

    /**
     * 发布时间起点
     */
    private Date time0;

    /**
     * 发布时间终点
     */
    private Date time1;

    /**
     * 校验查询参数
     */
    @Override
    public void validate(Errors errors) {
        validateEnum(errors, ArticleQueryParams::getType, ArticleType.class, "request.article.type");
        validateEnums(errors, ArticleQueryParams::getStatus, ArticleStatus.class, "request.article.status");
        validateTime(errors, ArticleQueryParams::getTime0, ArticleQueryParams::getTime1);
    }
}
