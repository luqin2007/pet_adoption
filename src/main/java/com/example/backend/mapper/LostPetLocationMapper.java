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

    default MPLambdaQuery<Location> queryByLocation(Location location) {
        return new MPLambdaQuery<>(this)
                .eq(Location::getProvince, location.getProvince())
                .eq(location.getCity() != null, Location::getCity, location.getCity())
                .eq(location.getDistrict() != null, Location::getDistrict, location.getDistrict())
                .select(Location::getParentId);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.lost_pet_location";
    }

    @Override
    default Class<Location> getEntityClass() {
        return Location.class;
    }
}

