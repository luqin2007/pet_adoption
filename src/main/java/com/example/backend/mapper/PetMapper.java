package com.example.backend.mapper;

import com.example.backend.dto.PetQueryParams;
import com.example.backend.entity.Location;
import com.example.backend.entity.Pet;
import com.example.backend.entity.property.PetStatus;
import com.example.backend.entity.query.PetLocations;
import com.example.backend.util.MPJLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

@Mapper
public interface PetMapper extends IBaseMapper<Pet> {

    default MPJLambdaQuery<Pet, ?, PetLocations> queryByParams(PetQueryParams params) {
        return this.<PetLocations>joinQuery()
                .in(Pet::getDiscoverId, params.getUser())
                .in(Pet::getAge, params.getAge0(), params.getAge1())
                .eq(Pet::getSex, params.getSex())
                .in(Pet::getType, params.getType())
                .in(Pet::getBreed, params.getBreed())
                .in(Pet::getStatus, PetStatus::get, params.getStatus())
                .like(Pet::getName, params.getName(), Pet::getDescription)
                .eq(Pet::getIsDiscard, params.getIsDiscard())
                // location
                .joinCollection(PetLocations.class, Location.class, Location::getParentId, Pet::getId, PetLocations::getLocations)
                .eq(Location::getProvince, params.getProvince())
                .eq(Location::getCity, params.getCity())
                .eq(Location::getDistrict, params.getDistrict())
                .like(Location::getDetailAddress, params.getAddress())
                .in(Location::getCreateTime, params.getTime(), null);
    }

    /**
     * 废弃宠物信息
     */
    default MPLambdaUpdate<Pet> discardPetById(Long id) {
        return lambdaUpdate()
                .eq(Pet::getId, id)
                .set(Pet::getIsDiscard, true);
    }

    default MPLambdaUpdate<Pet> updateStatus(Long petId, PetStatus status) {
        return lambdaUpdate()
                .eq(Pet::getId, petId)
                .set(Pet::getStatus, status)
                .set(Pet::getUpdateTime, new Date());
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.pet";
    }

    @Override
    default Class<Pet> getEntityClass() {
        return Pet.class;
    }
}

