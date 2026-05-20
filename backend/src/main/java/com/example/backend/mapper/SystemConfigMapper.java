package com.example.backend.mapper;

import com.example.backend.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemConfigMapper extends IBaseMapper<SystemConfig> {
    @Override
    default String getMissingMessage() {
        return "exception.not_found.system_config";
    }

    @Override
    default Class<SystemConfig> getEntityClass() {
        return SystemConfig.class;
    }
}
