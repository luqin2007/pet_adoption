package com.example.backend.mapper;

import com.example.backend.entity.ArticleFavorite;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (articleId, userId)
 * - (userId, createTime)
 */
@Mapper
public interface ArticleFavoriteMapper extends IBaseMapper<ArticleFavorite> {

    /**
     * 根据文章查询收藏记录
     */
    default MPLambdaQuery<ArticleFavorite> queryByArticle(Long articleId) {
        return lambdaQuery().eq(ArticleFavorite::getArticleId, articleId);
    }

    /**
     * 根据文章和用户查询收藏记录
     */
    default MPLambdaQuery<ArticleFavorite> queryByArticleAndUser(Long articleId, Long userId) {
        return lambdaQuery()
                .eq(ArticleFavorite::getArticleId, articleId)
                .eq(ArticleFavorite::getUserId, userId);
    }

    /**
     * 根据用户查询收藏记录
     */
    default MPLambdaQuery<ArticleFavorite> queryByUser(Long userId) {
        return lambdaQuery()
                .eq(ArticleFavorite::getUserId, userId)
                .desc(ArticleFavorite::getCreateTime);
    }

    /**
     * 根据用户和文章集合查询收藏记录
     */
    default MPLambdaQuery<ArticleFavorite> queryByUserAndArticles(Long userId, Set<Long> articleIds) {
        return lambdaQuery()
                .eq(ArticleFavorite::getUserId, userId)
                .in(ArticleFavorite::getArticleId, articleIds);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.article_favorite";
    }

    @Override
    default Class<ArticleFavorite> getEntityClass() {
        return ArticleFavorite.class;
    }
}
