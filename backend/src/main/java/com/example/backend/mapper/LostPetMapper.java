package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.example.backend.dto.LostPetQueryParams;
import com.example.backend.entity.Location;
import com.example.backend.entity.LostPet;
import com.example.backend.entity.Pet;
import com.example.backend.entity.property.LostPetStatus;
import com.example.backend.entity.property.ParentType;
import com.example.backend.entity.query.LostPetLocation;
import com.example.backend.util.MPJLambdaQuery;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import com.example.backend.util.StringUtils;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.Set;
import java.util.function.Consumer;

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
                .eq(Location::getParentType, ParentType.LOST_PET)
                .eq(Location::getProvince, params.getProvince())
                .eq(Location::getCity, params.getCity())
                .eq(Location::getDistrict, params.getDistrict())
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

    default MPLambdaQuery<LostPet> matchPet(Set<Long> lostPetIds, Pet pet) {
        return lambdaQuery()
                .in(LostPet::getId, lostPetIds)
                .eq(LostPet::getStatus, LostPetStatus.SEARCHING)
                .or(StringUtils.hasText(pet.getSex()),
                        compareNullable(LostPet::getSex, pet.getSex(), "未知"))
                .or(StringUtils.hasText(pet.getType()),
                        compareNullable(LostPet::getType, pet.getType(), null));
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

    @SuppressWarnings("unchecked")
    private Consumer<LambdaQueryWrapper<LostPet>>[] compareNullable(SFunction<LostPet, String> column, String value, String value2) {
        return new Consumer[] {
                wrapper -> ((LambdaQueryWrapper<LostPet>) wrapper).isNull(column),
                wrapper -> ((LambdaQueryWrapper<LostPet>) wrapper).eq(column, value),
                wrapper -> ((LambdaQueryWrapper<LostPet>) wrapper).eq(value2 != null, column, value2),
        };
    }
}
