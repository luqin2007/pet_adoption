package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.PetTag;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

/**
 * 索引：<br>
 * - (petId)
 */
@Mapper
public interface PetTagMapper extends IBaseMapper<PetTag> {

    /**
     * 查询某宠物所有 Tag<br>
     * - 索引：(petId)
     */
    default LambdaQueryWrapper<PetTag> queryByPet(Long petId) {
        return lambdaQuery().eq(PetTag::getPetId, petId);
    }

    /**
     * 批量宠物 Tag<br>
     * - 索引：(petId)
     */
    default LambdaQueryWrapper<PetTag> queryByPets(Collection<Long> petIds) {
        return lambdaQuery().in(PetTag::getPetId, petIds);
    }

    /**
     * 筛选某宠物的 Tag，用于删除<br>
     * - 索引：(petId)
     */
    default LambdaQueryWrapper<PetTag> deleteByPetAndIds(Long petId, Collection<Long> ids) {
        return lambdaQuery()
                .eq(PetTag::getPetId, petId)
                .in(PetTag::getId, ids);
    }

    @Override
    default String getMissingMessage() {
        return "标签信息不存在";
    }
}
