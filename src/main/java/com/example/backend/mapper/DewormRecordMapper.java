package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.DewormRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (petId, createTime)
 */
@Mapper
public interface DewormRecordMapper extends IBaseMapper<DewormRecord> {

    default LambdaQueryWrapper<DewormRecord> queryByPet(Long petId) {
        return new LambdaQueryWrapper<DewormRecord>()
                .eq(DewormRecord::getPetId, petId)
                .orderByDesc(DewormRecord::getCreateTime);
    }

    default LambdaQueryWrapper<DewormRecord> queryByPets(Set<Long> petIds) {
        return new LambdaQueryWrapper<DewormRecord>()
                .in(DewormRecord::getPetId, petIds)
                .orderByDesc(DewormRecord::getCreateTime);
    }

    @Override
    default String getMissingMessage() {
        return "驱虫记录不存在";
    }
}
