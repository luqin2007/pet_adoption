package com.example.backend.mapper;

import com.example.backend.dto.LostPetQueryParams;
import com.example.backend.entity.Location;
import com.example.backend.entity.LostPet;
import com.example.backend.entity.property.LostPetStatus;
import com.example.backend.entity.query.LostPetLocation;
import com.example.backend.util.MPJLambdaQuery;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.Set;

/**
 * 索引：
 * - (owner, status)
 * - (status)
 */
@Mapper
public interface LostPetMapper extends IBaseMapper<LostPet> {

    default MPJLambdaQuery<LostPet, Location, LostPetLocation> queryByRequestLocation(LostPetQueryParams params) {
        return new MPJLambdaQuery<LostPet, LostPet, LostPetLocation>(this)
                .in(LostPet::getOwnerId, params.getOwner())
                .in(LostPet::getStatus, LostPetStatus::get, params.getStatus())
                .in(LostPet::getType, params.getType())
                .in(LostPet::getBreed, params.getBread())
                .in(LostPet::getCreateTime, params.getTime0(), params.getTime1())
                .like(LostPet::getName, params.getName())
                .join(LostPetLocation.class, Location.class, Location::getParentId, LostPet::getId, LostPetLocation::getLocation)
                .eq(Location::getProvince, params.getProvince())
                .eq(Location::getCity, params.getCity())
                .like(Location::getDetailAddress, params.getAddress());
    }

    default MPLambdaQuery<LostPet> queryByRequest(LostPetQueryParams params) {
        return new MPLambdaQuery<>(this)
                .in(LostPet::getOwnerId, params.getOwner())
                .in(LostPet::getStatus, LostPetStatus::get, params.getStatus())
                .in(LostPet::getType, params.getType())
                .in(LostPet::getBreed, params.getBread())
                .in(LostPet::getCreateTime, params.getTime0(), params.getTime1())
                .like(LostPet::getName, params.getName());
    }

    default MPLambdaQuery<LostPet> filterLostPet(Set<Long> lostPetIds, Date findTime) {
        return new MPLambdaQuery<>(this)
                .in(LostPet::getId, lostPetIds)
                .eq(LostPet::getStatus, LostPetStatus.SEARCHING)
                .in(LostPet::getCreateTime, findTime, null);
    }

    default void updateStatus(Long id, Long petId, LostPetStatus status) {
        new MPLambdaUpdate<>(this)
                .eq(LostPet::getId, id)
                .set(petId != null && status == LostPetStatus.CLAIMED, LostPet::getPetId, petId)
                .set(LostPet::getStatus, status)
                .set(LostPet::getUpdateTime, new Date())
                .update();
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.lost_pet";
    }

    @Override
    default Class<LostPet> getEntityClass() {
        return LostPet.class;
    }
}

