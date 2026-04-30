package com.example.backend.mapper;

import com.example.backend.entity.Location;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.dto.LostPetQueryParams;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.Set;

/**
 * 索引：
 * - (parentType, parentId)
 */
@Mapper
public interface LocationMapper extends IBaseMapper<Location> {

    default MPLambdaQuery<Location> queryByParent(ParentType parentType, Long parentId) {
        return new MPLambdaQuery<>(this)
                .eq(Location::getParentType, parentType)
                .eq(Location::getParentId, parentId);
    }

    default MPLambdaQuery<Location> queryByParents(ParentType parentType, Set<Long> parentIds) {
        return new MPLambdaQuery<>(this)
                .eq(Location::getParentType, parentType)
                .in(Location::getParentId, parentIds);
    }

    default MPLambdaQuery<Location> queryByLostPetRequest(LostPetQueryParams params) {
        return new MPLambdaQuery<>(this)
                .eq(Location::getParentType, ParentType.LOST_PET)
                .eq(Location::getProvince, params.getProvince())
                .eq(Location::getCity, params.getCity())
                .like(Location::getDetailAddress, params.getAddress());
    }

    default MPLambdaQuery<Location> queryPetLocations(Location location, Date minTime) {
        return queryBySimilarLocation(ParentType.PET, location)
                .in(Location::getCreateTime, minTime, null);
    }

    default MPLambdaQuery<Location> queryLostPetLocations(Location location) {
        return queryBySimilarLocation(ParentType.LOST_PET, location);
    }

    private MPLambdaQuery<Location> queryBySimilarLocation(ParentType parentType, Location location) {
        return new MPLambdaQuery<>(this)
                .eq(Location::getParentType, parentType)
                .eq(Location::getProvince, location.getProvince())
                .eq(location.getCity() != null, Location::getCity, location.getCity())
                .eq(location.getDistrict() != null, Location::getDistrict, location.getDistrict())
                .select(Location::getParentId);
    }

    @Override
    default Class<Location> getEntityClass() {
        return Location.class;
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.location";
    }
}
