package com.example.backend.mapper;

import com.example.backend.entity.Location;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.Set;

/**
 * 索引：<br>
 * - (parentId)
 * - (province, city, district, createTime)
 */
@Mapper
public interface PetLocationMapper extends IBaseMapper<Location> {

    default MPLambdaQuery<Location> queryByPet(Long petId) {
        return lambdaQuery().eq(Location::getParentId, petId);
    }

    default MPLambdaQuery<Location> queryLostPets(Location location, Date minTime, Set<Long> ignoredIds) {
        return lambdaQuery()
                .eq(Location::getProvince, location.getProvince())
                .eq(location.getCity() != null, Location::getCity, location.getCity())
                .eq(location.getDistrict() != null, Location::getDistrict, location.getDistrict())
                .in(Location::getCreateTime, minTime, null)
                .notIn(Location::getId, ignoredIds);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.pet_location";
    }

    @Override
    default Class<Location> getEntityClass() {
        return Location.class;
    }
}

