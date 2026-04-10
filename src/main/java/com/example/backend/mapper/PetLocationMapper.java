package com.example.backend.mapper;

import com.example.backend.entity.Location;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * 索引：<br>
 * - (parentId)
 * - (province, city, county, createTime)
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
                .eq(location.getCounty() != null, Location::getCounty, location.getCounty())
                .in(Location::getCreateTime, minTime, null)
                .select(Location::getParentId);
    }

    @Override
    default String getMissingMessage() {
        return "宠物位置信息不存在";
    }

    @Override
    default Class<Location> getEntityClass() {
        return Location.class;
    }
}
