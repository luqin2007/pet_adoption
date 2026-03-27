package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.entity.Pet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetMapper extends IBaseMapper<Pet> {

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
