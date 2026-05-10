package com.example.backend.mapper;

import com.example.backend.dto.ArticleQueryParams;
import com.example.backend.entity.Article;
import com.example.backend.entity.property.ArticleStatus;
import com.example.backend.entity.property.ArticleType;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (status, publishTime)
 * - (authorId, createTime)
 * - (type, status)
 */
@Mapper
public interface ArticleMapper extends IBaseMapper<Article> {

    /**
     * 根据查询条件筛选文章
     */
    default MPLambdaQuery<Article> queryByRequest(ArticleQueryParams params) {
        return lambdaQuery()
                .eq(Article::getType, ArticleType::get, params.getType())
                .eq(Article::getAuthorId, params.getAuthor())
                .in(Article::getStatus, ArticleStatus::get, params.getStatus())
                .like(Article::getTitle, params.getTitle())
                .in(Article::getPublishTime, params.getTime0(), params.getTime1())
                .eq(Article::getIsDiscard, params.getIsDiscard())
                .desc(Article::getPublishTime)
                .desc(Article::getCreateTime);
    }

    default MPLambdaQuery<Article> getDisplayArticles(Set<Long> articleIds) {
        return lambdaQuery()
                .in(Article::getId, articleIds)
                .eq(Article::getStatus, ArticleStatus.PUBLISHED)
                .eq(Article::getIsDiscard, Boolean.FALSE);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.article";
    }

    @Override
    default Class<Article> getEntityClass() {
        return Article.class;
    }
}
