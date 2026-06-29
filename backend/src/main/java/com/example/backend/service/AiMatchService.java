package com.example.backend.service;

import com.example.backend.dto.ai.AIMatchResult;
import com.example.backend.entity.LostPet;
import com.example.backend.entity.Pet;
import com.example.backend.entity.PetFeatureCache;
import com.example.backend.entity.User;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.ServiceException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiMatchService extends BaseServiceWithoutMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiMatchService.class);

    private final SystemConfigService configService;
    private final UserSettingService userSettingService;
    private final PetFeatureService petFeatureService;
    private final AiChatService aiChatService;
    private final PetService petService;
    private LostPetService lostPetService;

    /**
     * 为单个实体提取特征并缓存
     */
    public String ensureFeaturesCached(ParentType parentType, Long parentId) {
        requireAi(parentType);
        String modelVersion = configService.getAiVersion();
        PetFeatureCache cached = petFeatureService.getCache(parentType, parentId, modelVersion);
        if (cached != null && cached.getFeatures() != null) {
            return cached.getFeatures();
        }

        String features;
        switch (parentType) {
            case LOST_PET:
                LostPet lost = lostPetService.requireById(parentId);
                features = aiChatService.extractPetFeatures(lost);
                petFeatureService.saveCache(parentType, parentId, modelVersion, features);
                return features;
            case PET:
                Pet pet = petService.requireById(parentId);
                features = aiChatService.extractPetFeatures(pet);
                petFeatureService.saveCache(parentType, parentId, modelVersion, features);
                return features;
            default:
                throw ServiceException.system("??????");
        }
    }

    /**
     * 批量缓存缺失特征
     */
    public void batchCacheFeatures(ParentType type, Set<Long> parentIds) {
        requireAi(type);
        String modelVersion = configService.getAiVersion();
        Set<Long> uncached = petFeatureService.getUncachedIds(type, parentIds, modelVersion);
        for (Long id : uncached) {
            try {
                ensureFeaturesCached(type, id);
            } catch (Exception e) {
                LOGGER.warn("Failed to cache features for {}/{}: {}", type, id, e.getMessage());
            }
        }
    }

    /**
     * 为走失宠物匹配流浪宠物（Top-N）
     */
    public List<Pet> matchLostPet(LostPet lostPet, List<Pet> pets) {
        requireAi(ParentType.LOST_PET);
        if (pets.isEmpty()) return List.of();
        String lostFeatures = ensureFeaturesCached(ParentType.LOST_PET, lostPet.getId());
        batchCacheFeatures(ParentType.PET, pets.stream().map(Pet::getId).collect(Collectors.toSet()));

        String modelVersion = configService.getAiVersion();
        List<AIMatchResult> results = new ArrayList<>();
        for (Pet pet : pets) {
            PetFeatureCache cache = petFeatureService.getCache(ParentType.PET, pet.getId(), modelVersion);
            if (cache == null || cache.getFeatures() == null) continue;

            try {
                String judgement = aiChatService.judgeMatch(lostFeatures, cache.getFeatures());
                results.add(parseMatchResult(lostPet, pet, judgement));
            } catch (Exception e) {
                LOGGER.warn("Failed to match pet {} with lost {}: {}", pet.getId(), lostPet.getId(), e.getMessage());
            }
        }
        return results.stream()
                .sorted()
                .map(AIMatchResult::getPet).toList();
    }

    /**
     * 为流浪宠物匹配走失宠物（Top-N）
     */
    public List<LostPet> matchPetToLost(Pet pet, List<LostPet> lostPets) {
        requireAi(ParentType.PET);
        if (lostPets.isEmpty()) return List.of();
        String petFeatures = ensureFeaturesCached(ParentType.PET, pet.getId());
        batchCacheFeatures(ParentType.LOST_PET, lostPets.stream().map(LostPet::getId).collect(Collectors.toSet()));

        String modelVersion = configService.getAiVersion();
        List<AIMatchResult> results = new ArrayList<>();
        for (LostPet lost : lostPets) {
            PetFeatureCache cache = petFeatureService.getCache(ParentType.LOST_PET, lost.getId(), modelVersion);
            if (cache == null || cache.getFeatures() == null) continue;

            try {
                String judgement = aiChatService.judgeMatch(cache.getFeatures(), petFeatures);
                results.add(parseMatchResult(lost, pet, judgement));
            } catch (Exception e) {
                LOGGER.warn("Failed to match lost {} with pet {}: {}", lost.getId(), pet.getId(), e.getMessage());
            }
        }
        return results.stream().sorted().map(AIMatchResult::getLostPet).toList();
    }

    /**
     * 增量移除缓存（实体信息更新时调用）
     */
    public void invalidateCache(ParentType parentType, Long parentId) {
        petFeatureService.deleteByParent(parentType, parentId);
    }

    private AIMatchResult parseMatchResult(LostPet lostPet, Pet pet, String json) {
        AIMatchResult result = new AIMatchResult();
        result.setLostPet(lostPet);
        result.setPet(pet);
        try {
            JsonNode root = objectMapper.readTree(json);
            result.setIsMatch(root.path("is_match").asBoolean(false));
            result.setConfidence(root.path("confidence").asDouble(0));
            List<String> reasons = new ArrayList<>();
            root.path("reasons").forEach(n -> reasons.add(n.asString()));
            result.setReasons(reasons);
        } catch (Exception e) {
            LOGGER.warn("Failed to parse match result JSON: {}", json, e);
            result.setIsMatch(false);
            result.setConfidence(0.0);
            result.setReasons(List.of("解析失败: " + e.getMessage()));
        }
        return result;
    }

    private void requireAi(ParentType parentType) {
        User login = requireLoginUser();
        boolean isAiEnable = configService.isAiEnabled()
                && Boolean.TRUE.equals(userSettingService.getOrCreate(login.getId()).getEnableAi());
        require(isAiEnable, "AI 匹配功能未启用");
        require(parentType.isPet(), "无效匹配类型");
    }

    @Autowired
    @Lazy
    public void setLostPetService(LostPetService lostPetService) {
        this.lostPetService = lostPetService;
    }

    public LostPetService getLostPetService() {
        return lostPetService;
    }
}
