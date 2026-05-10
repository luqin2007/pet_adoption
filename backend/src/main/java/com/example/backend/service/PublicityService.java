package com.example.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.example.backend.dto.*;
import com.example.backend.entity.Article;
import com.example.backend.entity.ArticleFavorite;
import com.example.backend.entity.ArticleLike;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ArticleStatus;
import com.example.backend.entity.property.ArticleType;
import com.example.backend.mapper.ArticleFavoriteMapper;
import com.example.backend.mapper.ArticleLikeMapper;
import com.example.backend.mapper.ArticleMapper;
import com.example.backend.util.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 公益宣传
 */
@Service
@RequiredArgsConstructor
public class PublicityService extends BaseService<ArticleMapper, Article> {

    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleFavoriteMapper articleFavoriteMapper;

    private UserService userService;

    @Value("${host.address}")
    private String hostAddress;

    /**
     * 创建文章
     */
    @Transactional
    public ArticleResponse addArticle(ArticleAddRequest request) {
        User login = requireLoginUser();
        Article article = request.create(login.getId());
        if (article.getType() == ArticleType.STORY) {
            requirePermission(login.isVolunteer());
        } else {
            requirePermission(login.isWorker());
        }

        save(article);
        return buildDetailResponse(article, login);
    }

    /**
     * 查询文章列表
     */
    public Page<ArticleResponse> getArticles(ArticleQueryParams query, PageParams page) {
        Optional<User> login = getLoginUser();
        if (login.isEmpty() // 未登录
                || (!login.get().isWorker() // 非工作人员
                && !login.get().is(query.getAuthor()))) { // 非本人
            query.setStatus(Set.of(ArticleStatus.PUBLISHED.name()));
            query.setIsDiscard(Boolean.FALSE);
        }

        Page<Article> result = baseMapper.queryByRequest(query).page(page);
        return buildPageResponse(result, login.orElse(null));
    }

    /**
     * 获取文章详情
     */
    @Transactional
    public ArticleResponse getArticle(Long articleId) {
        Article article = requireById(articleId);
        Optional<User> login = getLoginUser();
        boolean isAuthor = login.isPresent() && login.get().is(article.getAuthorId());
        if (article.getStatus() != ArticleStatus.PUBLISHED) // 未发布文章
            requirePermission(login.isPresent() && (isAuthor || login.get().isWorker()));
        if (!Boolean.FALSE.equals(article.getIsDiscard())) // 已删除文章
            requirePermission(login.isPresent() && (isAuthor || login.get().isWorker()));

        if (article.getStatus() == ArticleStatus.PUBLISHED) {
            article.setViewCount(article.getViewCount() + 1);
            article.setUpdateTime(new Date());
            updateById(article);
        }
        return buildDetailResponse(article, login.orElse(null));
    }

    /**
     * 修改文章
     */
    @Transactional
    public ArticleResponse updateArticle(Long articleId, ArticleUpdateRequest request) {
        User login = requireLoginUser();
        Article article = requireById(articleId);
        requirePermission(login.is(article.getAuthorId()));
        require(article.getStatus() == ArticleStatus.DRAFT, "exception.invalidate.article.not_draft");

        request.applyTo(article);
        updateById(article);
        return buildDetailResponse(article, login);
    }

    /**
     * 删除文章
     */
    @Transactional
    public void deleteArticle(Long articleId) {
        User login = requireLoginUser();
        Article article = requireById(articleId);
        requirePermission(login.is(article.getAuthorId()) || login.isWorker());
        require(article.getStatus() == ArticleStatus.DRAFT, "exception.invalidate.article.delete_published");

        articleLikeMapper.queryByArticle(articleId).delete();
        articleFavoriteMapper.queryByArticle(articleId).delete();
        article.setIsDiscard(true);
        updateById(article);
    }

    /**
     * 文章状态修改
     */
    public ArticleResponse updateArticleStatus(Long articleId, String statusName) {
        User login = requireLoginUser();
        Article article = requireById(articleId);
        ArticleStatus status = ArticleStatus.get(statusName);
        require(status.canChangeFrom(article.getStatus()), "exception.invalidate.status");
        if (status == ArticleStatus.OFFLINE)
            requirePermission(login.isWorker());
        else
            requirePermission(login.isWorker() || login.is(article.getAuthorId()));

        article.setStatus(status);
        updateById(article);
        return buildDetailResponse(article, login);
    }

    /**
     * 点赞文章
     */
    @Transactional
    public ArticleLikeResponse likeArticle(Long articleId, boolean liked) {
        Article article = requireById(articleId);
        require(article.getStatus() == ArticleStatus.PUBLISHED, "exception.invalidate.article.not_published");

        User login = requireLoginUser();
        if (liked == articleLikeMapper.queryByArticleAndUser(articleId, login.getId()).exists()) {
            throw ServiceException.conflict("exception.conflict.article.duplicate_action");
        }

        if (liked) {
            articleLikeMapper.insert(new ArticleLike(null, articleId, login.getId(), new Date()));
            article.setLikeCount(article.getLikeCount() + 1);
        } else {
            articleLikeMapper.queryByArticleAndUser(articleId, login.getId()).delete();
            article.setLikeCount(Math.max(0, article.getLikeCount() - 1));
        }
        article.setUpdateTime(new Date());
        updateById(article);
        return buildLikeResponse(article, login);
    }

    /**
     * 收藏文章
     */
    @Transactional
    public void favoriteArticle(Long articleId, boolean favorited) {
        Article article = requireById(articleId);
        require(article.getStatus() == ArticleStatus.PUBLISHED, "exception.invalidate.article.not_published");

        User login = requireLoginUser();
        if (favorited == articleFavoriteMapper.queryByArticleAndUser(articleId, login.getId()).exists()) {
            throw ServiceException.conflict("exception.conflict.article.duplicate_action");
        }

        if (favorited) {
            articleFavoriteMapper.insert(new ArticleFavorite(null, articleId, login.getId(), new Date()));
        } else {
            articleFavoriteMapper.queryByArticleAndUser(articleId, login.getId()).delete();
        }
    }

    /**
     * 获取我的收藏列表
     */
    public Page<ArticleResponse> getFavorites(PageParams page) {
        User login = requireLoginUser();
        PageParams pageParams = page == null ? new PageParams() : page;

        Page<ArticleFavorite> favorites = articleFavoriteMapper.queryByUser(login.getId()).page(pageParams);

        Set<Long> articleIds = favorites.getRecords().stream()
                .map(ArticleFavorite::getArticleId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, Article> articles = baseMapper
                .getDisplayArticles(articleIds)
                .groupById();
        Map<Long, User> authors = userService.groupById(
                articles.values().stream().map(Article::getAuthorId),
                User::getId,
                User::getUsername,
                User::getAvatar);
        Set<Long> likedIds = getLikedArticleIds(articleIds, login);

        return convertDto(favorites, favorite -> {
            Article article = articles.get(favorite.getArticleId());
            ArticleResponse item = ArticleResponse.createBatch(article, authors, false);
            item.setContent(null);
            item.setLiked(likedIds.contains(article.getId()));
            item.setFavorited(Boolean.TRUE);
            return item;
        });
    }

    /**
     * 获取分享信息
     */
    @Transactional
    public ArticleShareResponse shareArticle(Long articleId) {
        Article article = requireById(articleId);
        require(article.getStatus() == ArticleStatus.PUBLISHED, "exception.invalidate.article.not_published");

        article.setShareCount(article.getShareCount() + 1);
        article.setUpdateTime(new Date());
        updateById(article);

        String detailUrl = hostAddress + "/publicity/articles/" + articleId;
        return new ArticleShareResponse(articleId, article.getTitle(), detailUrl);
    }

    /**
     * 构造文章分页响应
     */
    private Page<ArticleResponse> buildPageResponse(Page<Article> result, User login) {
        Page<ArticleResponse> response = PageDTO.of(result.getCurrent(), result.getSize(), result.getTotal());
        if (result.getRecords().isEmpty()) {
            response.setRecords(java.util.List.of());
            return response;
        }

        Set<Long> articleIds = result.getRecords().stream()
                .map(Article::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Map<Long, User> authors = userService.groupById(
                result.getRecords().stream().map(Article::getAuthorId),
                User::getId,
                User::getUsername,
                User::getAvatar);
        Set<Long> likedIds = getLikedArticleIds(articleIds, login);
        Set<Long> favoritedIds = getFavoritedArticleIds(articleIds, login);
        response.setRecords(result.getRecords().stream()
                .map(article -> {
                    ArticleResponse item = ArticleResponse.createBatch(article, authors, true);
                    item.setContent(null);
                    item.setLiked(likedIds.contains(article.getId()));
                    item.setFavorited(favoritedIds.contains(article.getId()));
                    return item;
                })
                .toList());
        return response;
    }

    /**
     * 构造文章详情响应
     */
    private ArticleResponse buildDetailResponse(Article article, User login) {
        User author = login != null && login.is(article.getAuthorId())
                ? login
                : userService.selectById(article.getAuthorId(), User::getId, User::getUsername, User::getAvatar);
        ArticleResponse response = ArticleResponse.create(article, author, false);
        if (login != null) {
            response.setLiked(articleLikeMapper.queryByArticleAndUser(article.getId(), login.getId()).exists());
            response.setFavorited(articleFavoriteMapper.queryByArticleAndUser(article.getId(), login.getId()).exists());
        }
        return response;
    }

    /**
     * 构造文章详情响应
     */
    private ArticleLikeResponse buildLikeResponse(Article article, User user) {
        boolean liked = articleLikeMapper.queryByArticleAndUser(article.getId(), user.getId()).exists();
        return new ArticleLikeResponse(article.getId(), user.getId(), liked);
    }

    /**
     * 获取当前用户已点赞的文章 id
     */
    private Set<Long> getLikedArticleIds(Set<Long> articleIds, User login) {
        if (login == null || articleIds.isEmpty()) {
            return Set.of();
        }
        return articleLikeMapper.queryByUserAndArticles(login.getId(), articleIds)
                .list(ArticleLike::getArticleId)
                .collect(Collectors.toSet());
    }

    /**
     * 获取当前用户已收藏的文章 id
     */
    private Set<Long> getFavoritedArticleIds(Set<Long> articleIds, User login) {
        if (login == null || articleIds.isEmpty()) {
            return Set.of();
        }
        return articleFavoriteMapper.queryByUserAndArticles(login.getId(), articleIds)
                .list(ArticleFavorite::getArticleId)
                .collect(Collectors.toSet());
    }

    @Autowired
    public void setServices(UserService userService) {
        this.userService = userService;
    }
}
