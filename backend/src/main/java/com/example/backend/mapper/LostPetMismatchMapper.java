package com.example.backend.mapper;

import com.example.backend.entity.LostPetMismatch;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (lostPetId, petId)
 * - (petId)
 */
@Mapper
public interface LostPetMismatchMapper extends IBaseMapper<LostPetMismatch> {

    default MPLambdaQuery<LostPetMismatch> queryByLostPetAndPet(Long lostPetId, Long petId) {
        return lambdaQuery()
                .eq(LostPetMismatch::getLostPetId, lostPetId)
                .eq(LostPetMismatch::getPetId, petId);
    }

    default MPLambdaQuery<LostPetMismatch> queryByPet(Long petId) {
        return lambdaQuery().eq(LostPetMismatch::getPetId, petId);
    }

    default MPLambdaQuery<LostPetMismatch> queryByLostPet(Long lostPetId) {
        return lambdaQuery().eq(LostPetMismatch::getLostPetId, lostPetId);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.lost_pet_mismatch";
    }

    @Override
    default Class<LostPetMismatch> getEntityClass() {
        return LostPetMismatch.class;
    }
}
