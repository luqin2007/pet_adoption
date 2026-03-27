package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.PetStatusRecord;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.util.ObjectUtils;

import java.util.Collection;

/**
 * 索引：<br>
 * - (petId, userId, createTime)
 */
@Mapper
public interface PetStatusRecordMapper extends IBaseMapper<PetStatusRecord> {

    /**
     * 获取特定用户提交的宠物状态记录<br>
     * - 索引：(petId, userId)
     *
     * @param userIds 用户 id, 为空则查询所有用户
     */
    default LambdaQueryWrapper<PetStatusRecord> queryByPet(Long petId, Collection<Long> userIds) {
        LambdaQueryWrapper<PetStatusRecord> wrapper = lambdaQuery()
                .eq(PetStatusRecord::getPetId, petId);
        return ObjectUtils.isEmpty(userIds) ? wrapper : wrapper.in(PetStatusRecord::getUserId, userIds);
    }

    @Override
    default String getMissingMessage() {
        return "状态记录不存在";
    }
}
