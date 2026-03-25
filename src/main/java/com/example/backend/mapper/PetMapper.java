package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.entity.Pet;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

@Mapper
public interface PetMapper extends IBaseMapper<Pet> {

    /**
     * 获取宠物 性别、类型、品种
     */
    default LambdaQueryWrapper<Pet> selectSexTypeBreedById(Long id) {
        return lambdaQuery()
                .eq(Pet::getId, id)
                .select(Pet::getId, Pet::getSex, Pet::getType, Pet::getBreed);
    }

    /**
     * 获取宠物 性别、类型、品种
     */
    default LambdaQueryWrapper<Pet> selectSexTypeBreedByIds(Set<Long> ids) {
        return lambdaQuery()
                .in(Pet::getId, ids)
                .select(Pet::getId, Pet::getSex, Pet::getType, Pet::getBreed);
    }

    /**
     * 获取宠物名称
     */
    default LambdaQueryWrapper<Pet> selectNameById(Long id) {
        return lambdaQuery()
                .eq(Pet::getId, id)
                .select(Pet::getId, Pet::getName);
    }

    /**
     * 获取宠物名称
     */
    default LambdaQueryWrapper<Pet> selectNameByIds(Set<Long> ids) {
        return lambdaQuery()
                .in(Pet::getId, ids)
                .select(Pet::getId, Pet::getName);
    }

    /**
     * 废弃宠物信息
     */
    default LambdaUpdateWrapper<Pet> discardPetById(Long id) {
        return lambdaUpdate()
                .eq(Pet::getId, id)
                .set(Pet::getIsDiscard, true);
    }

    @Override
    default String getMissingMessage() {
        return "宠物不存在";
    }
}
