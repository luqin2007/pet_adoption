package com.example.backend.mapper;

import com.example.backend.entity.Location;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VolunteerLocationMapper extends IBaseMapper<Location> {

    default MPLambdaQuery<Location> queryByParent(Long parentId) {
        return lambdaQuery().eq(Location::getParentId, parentId);
    }

    @Override
    default Class<Location> getEntityClass() {
        return Location.class;
    }
}
