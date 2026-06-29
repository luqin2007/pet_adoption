package com.example.backend.controller;

import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.service.AiMatchService;
import com.example.backend.service.PetFeatureService;
import com.example.backend.util.RedisHelper;
import com.example.backend.util.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.example.backend.dto.Result;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final PetFeatureService petFeatureService;
    private final AiMatchService aiMatchService;
    private final RedisHelper redisHelper;

    /**
     * 清空所有 AI 缓存（仅工作人员）
     */
    @DeleteMapping("/cached-features")
    public Result<Void> deleteCachedFeatures() {
        User login = aiMatchService.requireLoginUser();
        aiMatchService.require(login.isWorker(), "权限不足");

        petFeatureService.deleteAll();
        redisHelper.deletePattern("ai:match:result:*");

        return Result.success(null);
    }

    /**
     * 删除指定走失宠物的缓存（工作人员或报备人）
     */
    @DeleteMapping("/cached-result/{lostPetId}")
    public Result<Void> deleteCachedResult(@PathVariable("lostPetId") Long lostPetId) {
        User login = aiMatchService.requireLoginUser();
        var lostPet = aiMatchService.getLostPetService().requireById(lostPetId);
        aiMatchService.require(login.isWorker() || login.is(lostPet.getOwnerId()), "权限不足");

        // 10min 冷却检查
        String cooldownKey = "ai:refresh:cooldown:" + lostPetId;
        if (redisHelper.hasString(cooldownKey)) {
            throw ServiceException.request("操作过于频繁，请稍后再试");
        }

        // 设置冷却 10min
        redisHelper.putStringSec(cooldownKey, "1", 600);

        // 清除特征缓存 + Redis 结果缓存
        petFeatureService.deleteByParent(ParentType.LOST_PET, lostPetId);
        String resultKey = "ai:match:result:lost:" + lostPetId;
        redisHelper.deleteString(resultKey);

        return Result.success(null);
    }
}
