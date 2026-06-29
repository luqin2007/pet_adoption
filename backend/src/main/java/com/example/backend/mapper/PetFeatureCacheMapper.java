package com.example.backend.mapper;

import com.example.backend.entity.PetFeatureCache;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetFeatureCacheMapper extends IBaseMapper<PetFeatureCache> {

    @Override
    default String getMissingMessage() {
        return "exception.not_found.feature_cache";
    }

    @Override
    default Class<PetFeatureCache> getEntityClass() {
        return PetFeatureCache.class;
    }
}
