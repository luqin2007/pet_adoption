package com.example.backend.mapper;

import com.example.backend.entity.AiTokenUsage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AiTokenUsageMapper extends IBaseMapper<AiTokenUsage> {

    @Override
    default String getMissingMessage() {
        return "exception.not_found.token_usage";
    }

    @Override
    default Class<AiTokenUsage> getEntityClass() {
        return AiTokenUsage.class;
    }
}
