package com.example.backend.mapper;

import com.example.backend.entity.ArticleLike;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (articleId, userId)
 * - (userId, createTime)
 */
@Mapper
public interface ArticleLikeMapper extends IBaseMapper<ArticleLike> {

    /**
     * 根据文章查询点赞记录
     */
    default MPLambdaQuery<ArticleLike> queryByArticle(Long articleId) {
        return lambdaQuery().eq(ArticleLike::getArticleId, articleId);
    }

    /**
     * 根据文章和用户查询点赞记录
     */
    default MPLambdaQuery<ArticleLike> queryByArticleAndUser(Long articleId, Long userId) {
        return lambdaQuery()
                .eq(ArticleLike::getArticleId, articleId)
                .eq(ArticleLike::getUserId, userId);
    }

    /**
     * 根据用户查询点赞记录
     */
    default MPLambdaQuery<ArticleLike> queryByUser(Long userId) {
        return lambdaQuery()
                .eq(ArticleLike::getUserId, userId)
                .desc(ArticleLike::getCreateTime);
    }

    /**
     * 根据用户和文章集合查询点赞记录
     */
    default MPLambdaQuery<ArticleLike> queryByUserAndArticles(Long userId, Set<Long> articleIds) {
        return lambdaQuery()
                .eq(ArticleLike::getUserId, userId)
                .in(ArticleLike::getArticleId, articleIds);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.article_like";
    }

    @Override
    default Class<ArticleLike> getEntityClass() {
        return ArticleLike.class;
    }
}
