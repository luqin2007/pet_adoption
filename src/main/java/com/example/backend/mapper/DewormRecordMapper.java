package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.DewormRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：<br>
 * - (petId, createTime)
 */
@Mapper
public interface DewormRecordMapper extends IBaseMapper<DewormRecord> {

    /**
     * 倒序获取宠物驱虫记录<br>
     * 索引：(petId, createTime)
     */
    default LambdaQueryWrapper<DewormRecord> queryByPet(Long petId) {
        return new LambdaQueryWrapper<DewormRecord>()
                .eq(DewormRecord::getPetId, petId)
                .orderByDesc(DewormRecord::getCreateTime);
    }

    /**
     * 倒序获取宠物驱虫记录<br>
     * 索引：(petId, createTime)
     */
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
