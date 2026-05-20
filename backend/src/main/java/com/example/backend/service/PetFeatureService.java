package com.example.backend.service;

import com.example.backend.entity.PetFeatureCache;
import com.example.backend.entity.property.ParentType;
import com.example.backend.mapper.PetFeatureCacheMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PetFeatureService extends BaseService<PetFeatureCacheMapper, PetFeatureCache> {

    public PetFeatureCache getCache(ParentType parentType, Long parentId, String modelVersion) {
        return baseMapper.lambdaQuery()
                .eq(PetFeatureCache::getParentType, parentType)
                .eq(PetFeatureCache::getParentId, parentId)
                .eq(PetFeatureCache::getModelVersion, modelVersion)
                .one();
    }

    public void saveCache(ParentType parentType, Long parentId, String modelVersion, String features) {
        PetFeatureCache existing = getCache(parentType, parentId, modelVersion);
        Date now = new Date();
        if (existing == null) {
            existing = new PetFeatureCache(null, parentId, parentType, modelVersion, features, null, now, now);
            baseMapper.insert(existing);
        } else {
            existing.setFeatures(features);
            existing.setUpdatedAt(now);
            baseMapper.updateById(existing);
        }
    }

    /**
     * 按 parentType + parentIds 批量获取缓存
     */
    public List<PetFeatureCache> getCaches(ParentType parentType, Set<Long> parentIds, String modelVersion) {
        if (parentIds == null || parentIds.isEmpty()) return List.of();
        return baseMapper.lambdaQuery()
                .eq(PetFeatureCache::getParentType, parentType)
                .in(PetFeatureCache::getParentId, parentIds)
                .eq(PetFeatureCache::getModelVersion, modelVersion)
                .list();
    }

    /**
     * 删除指定 modelVersion 的全部缓存（切换模型时调用）
     */
    public void deleteByModelVersion(String modelVersion) {
        baseMapper.lambdaQuery()
                .eq(PetFeatureCache::getModelVersion, modelVersion)
                .delete();
    }

    /**
     * 删除指定 parent 的缓存
     */
    public void deleteByParent(ParentType parentType, Long parentId) {
        baseMapper.lambdaQuery()
                .eq(PetFeatureCache::getParentType, parentType)
                .eq(PetFeatureCache::getParentId, parentId)
                .delete();
    }

    /**
     * 获取所有未缓存的特征（缺少缓存的 parentId 集合）
     */
    public Set<Long> getUncachedIds(ParentType parentType, Set<Long> parentIds, String modelVersion) {
        if (parentIds == null || parentIds.isEmpty()) return Set.of();
        List<PetFeatureCache> cached = getCaches(parentType, parentIds, modelVersion);
        Set<Long> cachedIds = cached.stream().map(PetFeatureCache::getParentId).collect(Collectors.toSet());
        return parentIds.stream().filter(id -> !cachedIds.contains(id)).collect(Collectors.toSet());
    }
}
