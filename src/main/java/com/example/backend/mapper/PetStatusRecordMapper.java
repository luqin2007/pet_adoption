package com.example.backend.mapper;

import com.example.backend.entity.PetStatusRecord;
import com.example.backend.util.MPLambdaQuery;
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
    default MPLambdaQuery<PetStatusRecord> queryByPet(Long petId, Collection<Long> userIds) {
        MPLambdaQuery<PetStatusRecord> wrapper = lambdaQuery()
                .eq(PetStatusRecord::getPetId, petId);
        return ObjectUtils.isEmpty(userIds) ? wrapper : wrapper.in(PetStatusRecord::getUserId, userIds);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.pet_status_record";
    }

    @Override
    default Class<PetStatusRecord> getEntityClass() {
        return PetStatusRecord.class;
    }
}

