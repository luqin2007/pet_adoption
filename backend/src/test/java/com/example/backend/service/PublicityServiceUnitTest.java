package com.example.backend.service;

import com.example.backend.dto.ArticleAddRequest;
import com.example.backend.dto.ArticleResponse;
import com.example.backend.dto.ArticleShareResponse;
import com.example.backend.entity.Article;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ArticleStatus;
import com.example.backend.entity.property.ArticleType;
import com.example.backend.entity.property.UserRole;
import com.example.backend.mapper.ArticleFavoriteMapper;
import com.example.backend.mapper.ArticleLikeMapper;
import com.example.backend.mapper.ArticleMapper;
import com.example.backend.util.CustomUserDetails;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.ServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PublicityServiceUnitTest {

    private ArticleMapper articleMapper;
    private ArticleLikeMapper articleLikeMapper;
    private ArticleFavoriteMapper articleFavoriteMapper;
    private PublicityService service;

    @BeforeEach
    void setUp() {
        articleMapper = mock(ArticleMapper.class);
        articleLikeMapper = mock(ArticleLikeMapper.class);
        articleFavoriteMapper = mock(ArticleFavoriteMapper.class);
        service = new PublicityService(articleLikeMapper, articleFavoriteMapper);
        ReflectionTestUtils.setField(service, "baseMapper", articleMapper);
        ReflectionTestUtils.setField(service, "hostAddress", "http://127.0.0.1:8080");
        when(articleMapper.updateById(any(Article.class))).thenReturn(1);
        when(articleMapper.insert(any(Article.class))).thenReturn(1);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void workerCanAddKnowledgeArticleAndReceivesDraftDetail() {
        login(user(5L, "worker", UserRole.WORKER.getMask()));
        stubArticleReactionState(false, false);
        ArticleAddRequest request = new ArticleAddRequest();
        request.setType("KNOWLEDGE");
        request.setTitle("春季照护");
        request.setContent("内容");
        request.setPublish(false);

        ArticleResponse response = service.addArticle(request);

        assertEquals("春季照护", response.getTitle());
        assertEquals(ArticleStatus.DRAFT, response.getStatus());
        assertEquals(5L, response.getAuthorId());
        assertEquals("worker", response.getAuthorName());
        verify(articleMapper).insert(argThat((Article article) ->
                article.getType() == ArticleType.KNOWLEDGE
                        && article.getStatus() == ArticleStatus.DRAFT
                        && article.getAuthorId().equals(5L)));
    }

    @Test
    void normalUserCannotAddKnowledgeArticle() {
        login(user(6L, "normal", UserRole.NORMAL.getMask()));
        ArticleAddRequest request = new ArticleAddRequest();
        request.setType("KNOWLEDGE");
        request.setTitle("春季照护");
        request.setContent("内容");

        ServiceException ex = assertThrows(ServiceException.class, () -> service.addArticle(request));

        assertEquals(ServiceException.E_AUTH, ex.getCode());
        assertEquals("exception.auth.denied", ex.getMessage());
        verify(articleMapper, never()).insert(any(Article.class));
    }

    @Test
    void shareArticleRequiresPublishedArticleAndIncrementsShareCount() {
        Article article = article(9L, ArticleStatus.PUBLISHED);
        article.setShareCount(2);
        when(articleMapper.requireById(9L)).thenReturn(article);

        ArticleShareResponse response = service.shareArticle(9L);

        assertEquals(9L, response.getArticleId());
        assertEquals("流浪动物科普", response.getTitle());
        assertEquals("http://127.0.0.1:8080/publicity/articles/9", response.getDetailUrl());
        assertEquals(3, article.getShareCount());
        assertNotNull(article.getUpdateTime());
        verify(articleMapper).updateById(article);
    }

    @Test
    void shareArticleRejectsDraftArticle() {
        Article article = article(10L, ArticleStatus.DRAFT);
        when(articleMapper.requireById(10L)).thenReturn(article);

        ServiceException ex = assertThrows(ServiceException.class, () -> service.shareArticle(10L));

        assertEquals(ServiceException.E_INVALIDATE, ex.getCode());
        assertEquals("exception.invalidate.article.not_published", ex.getMessage());
        verify(articleMapper, never()).updateById(any(Article.class));
    }

    @SuppressWarnings("unchecked")
    private void stubArticleReactionState(boolean liked, boolean favorited) {
        MPLambdaQuery<?> likeQuery = mock(MPLambdaQuery.class);
        MPLambdaQuery<?> favoriteQuery = mock(MPLambdaQuery.class);
        when(likeQuery.exists()).thenReturn(liked);
        when(favoriteQuery.exists()).thenReturn(favorited);
        when(articleLikeMapper.queryByArticleAndUser(any(), any())).thenReturn((MPLambdaQuery) likeQuery);
        when(articleFavoriteMapper.queryByArticleAndUser(any(), any())).thenReturn((MPLambdaQuery) favoriteQuery);
    }

    private static void login(User user) {
        SecurityContextHolder.getContext()
                .setAuthentication(new TestingAuthenticationToken(new CustomUserDetails(user), null));
    }

    private static User user(Long id, String username, int role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword("{noop}pwd");
        user.setEmail(username + "@example.com");
        user.setRole(role);
        return user;
    }

    private static Article article(Long id, ArticleStatus status) {
        return new Article(id,
                ArticleType.KNOWLEDGE,
                status,
                "流浪动物科普",
                "正文",
                null,
                5L,
                Boolean.FALSE,
                0,
                0,
                0,
                status == ArticleStatus.PUBLISHED ? new Date() : null,
                new Date(),
                new Date());
    }
}
