package com.example.backend.mapper;

import com.example.backend.dto.LostPetQueryParams;
import com.example.backend.entity.Location;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (parentId)
 */
@Mapper
public interface LostPetLocationMapper extends IBaseMapper<Location> {

    default MPLambdaQuery<Location> queryByLostPets(Set<Long> petIds) {
        return new MPLambdaQuery<>(this).in(Location::getParentId, petIds);
    }

    default MPLambdaQuery<Location> queryByRequest(LostPetQueryParams params) {
        return new MPLambdaQuery<>(this)
                .eq(Location::getProvince, params.getProvince())
                .eq(Location::getCity, params.getCity())
                .like(Location::getDetailAddress, params.getAddress());
    }

    @Override
    default String getMissingMessage() {
        return "找不到位置信息";
    }

    @Override
    default Class<Location> getEntityClass() {
        return Location.class;
    }
}
