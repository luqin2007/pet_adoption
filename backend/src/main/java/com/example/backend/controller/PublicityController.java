package com.example.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.*;
import com.example.backend.service.PublicityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公益宣传模块<br>
 * - 救助故事、活动推广、科普知识<br>
 * ---- 创建文章 addArticle ( √ × )<br>
 * ---- 查询文章 getArticles ( √ × )<br>
 * ---- 获取文章 getArticle ( √ × )<br>
 * ---- 修改文章 updateArticle ( √ × )<br>
 * ---- 删除文章 deleteArticle ( √ × )<br>
 * ---- 文章状态修改 updateArticleStatus ( √ × )<br>
 * ---- 点赞文章 likeArticle ( √ × )<br>
 * ---- 取消点赞 unlikeArticle ( √ × )<br>
 * ---- 收藏文章 favoriteArticle ( √ × )<br>
 * ---- 取消收藏 unfavoriteArticle ( √ × )<br>
 * ---- 查询收藏 getFavorites ( √ × )<br>
 * - 分享功能<br>
 * ---- 分享文章 shareArticle ( √ × )<br>
 */
@Validated
@RestController
@RequestMapping("/api/v1/publicity")
@RequiredArgsConstructor
public class PublicityController {

    private final PublicityService publicityService;

    /**
     * 创建文章
     */
    @PostMapping("/articles")
    public Result<ArticleResponse> addArticle(@Valid @RequestBody ArticleAddRequest request) {
        ArticleResponse response = publicityService.addArticle(request);
        return Result.success(response);
    }

    /**
     * 查询文章列表
     */
    @GetMapping("/articles")
    public Result<Page<ArticleResponse>> getArticles(@Valid ArticleQueryParams query, PageParams page) {
        Page<ArticleResponse> response = publicityService.getArticles(query, page);
        return Result.success(response);
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/articles/{id}")
    public Result<ArticleResponse> getArticle(@PathVariable("id") Long articleId) {
        ArticleResponse response = publicityService.getArticle(articleId);
        return Result.success(response);
    }

    /**
     * 修改文章
     */
    @PutMapping("/articles/{id}")
    public Result<ArticleResponse> updateArticle(@PathVariable("id") Long articleId,
                                                 @Valid @RequestBody ArticleUpdateRequest request) {
        ArticleResponse response = publicityService.updateArticle(articleId, request);
        return Result.success(response);
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/articles/{id}")
    public Result<Void> deleteArticle(@PathVariable("id") Long articleId) {
        publicityService.deleteArticle(articleId);
        return Result.success();
    }

    /**
     * 文章状态修改
     */
    @PatchMapping("/articles/{id}/{st}")
    public Result<ArticleResponse> updateArticleStatus(@PathVariable("id") Long articleId,
                                                       @PathVariable("st") String status) {
        ArticleResponse response = publicityService.updateArticleStatus(articleId, status);
        return Result.success(response);
    }

    /**
     * 点赞文章
     */
    @PostMapping("/articles/{id}/like")
    public Result<ArticleLikeResponse> likeArticle(@PathVariable("id") Long articleId) {
        ArticleLikeResponse response = publicityService.likeArticle(articleId, true);
        return Result.success(response);
    }

    /**
     * 取消点赞
     */
    @DeleteMapping("/articles/{id}/like")
    public Result<ArticleLikeResponse> unlikeArticle(@PathVariable("id") Long articleId) {
        ArticleLikeResponse response = publicityService.likeArticle(articleId, false);
        return Result.success(response);
    }

    /**
     * 收藏文章
     */
    @PostMapping("/articles/{id}/favorite")
    public Result<Void> favoriteArticle(@PathVariable("id") Long articleId) {
        publicityService.favoriteArticle(articleId, true);
        return Result.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/articles/{id}/favorite")
    public Result<Void> unfavoriteArticle(@PathVariable("id") Long articleId) {
        publicityService.favoriteArticle(articleId, false);
        return Result.success();
    }

    /**
     * 我的收藏列表
     */
    @GetMapping("/favorites")
    public Result<Page<ArticleResponse>> getFavorites(PageParams page) {
        return Result.success(publicityService.getFavorites(page));
    }

    /**
     * 获取文章分享信息
     */
    @PostMapping("/articles/{id}/share")
    public Result<ArticleShareResponse> shareArticle(@PathVariable("id") Long articleId) {
        return Result.success(publicityService.shareArticle(articleId));
    }
}
