package com.example.backend.mapper;

import com.example.backend.entity.Location;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

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

    /**
     * 返回：[(parentId)]
     */
    default MPLambdaQuery<Location> queryByLocation(Location location, Date minTime) {
        return lambdaQuery()
                .eq(Location::getProvince, location.getProvince())
                .eq(location.getCity() != null, Location::getCity, location.getCity())
                .eq(location.getDistrict() != null, Location::getDistrict, location.getDistrict())
                .in(Location::getCreateTime, minTime, null)
                .select(Location::getParentId);
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

