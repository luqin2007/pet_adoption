package com.example.backend.mapper;

import com.example.backend.entity.Location;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (parentType, parentId)
 */
@Mapper
public interface PetLocationMapper extends IBaseMapper<Location> {
}
