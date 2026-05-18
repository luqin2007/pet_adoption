package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.VaccineRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：<br>
 * - (petId, createTime)
 */
@Mapper
public interface VaccineRecordMapper extends IBaseMapper<VaccineRecord> {

    /**
     * 获取宠物的疫苗记录（倒序）
     */
    default LambdaQueryWrapper<VaccineRecord> queryByPet(Long petId) {
        return new LambdaQueryWrapper<VaccineRecord>()
                .eq(VaccineRecord::getPetId, petId)
                .orderByDesc(VaccineRecord::getCreateTime);
    }

    /**
     * 获取宠物的疫苗记录（倒序）
     */
    default LambdaQueryWrapper<VaccineRecord> queryByPets(Set<Long> petIds) {
        return new LambdaQueryWrapper<VaccineRecord>()
                .in(VaccineRecord::getPetId, petIds)
                .orderByDesc(VaccineRecord::getCreateTime);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.vaccine_record";
    }

    @Override
    default Class<VaccineRecord> getEntityClass() {
        return VaccineRecord.class;
    }
}

