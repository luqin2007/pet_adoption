package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.PetTag;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;

/**
 * 索引：
 * - 查找某模块资源 : (petId)
 */
@Mapper
public interface PetTagMapper extends IBaseMapper<PetTag> {

    /**
     * 查询某宠物所有 Tag
     * - 索引：(petId)
     * - 查询：[PetTag]
     *
     * @param petId 流浪宠物 id
     */
    default LambdaQueryWrapper<PetTag> queryByPet(Long petId) {
        return lambdaQuery().eq(PetTag::getPetId, petId);
    }

    /**
     * 批量宠物 Tag
     * - 索引：(petId)
     * - 查询：[PetTag]
     *
     * @param petIds 流浪宠物 id
     */
    default LambdaQueryWrapper<PetTag> queryByPets(Collection<Long> petIds) {
        return lambdaQuery().in(PetTag::getPetId, petIds);
    }

    /**
     * 筛选某宠物的 Tag，用于删除
     * - 索引：(petId)
     *
     * @param petId 流浪宠物 id
     * @param ids   Tag id
     */
    default LambdaQueryWrapper<PetTag> deleteByIdsFilterByPet(Long petId, Collection<Long> ids) {
        return lambdaQuery()
                .eq(PetTag::getPetId, petId)
                .in(PetTag::getId, ids);
    }
}
