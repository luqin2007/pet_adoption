package com.example.backend.mapper;

import com.example.backend.dto.FirstRegistrationQueryParams;
import com.example.backend.entity.FirstRegistration;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引<br>
 * - (petId, createTime)<br>
 * - (userId, createTime)
 */
@Mapper
public interface FirstRegistrationMapper extends IBaseMapper<FirstRegistration> {

    /**
     * 根据宠物 id 查询初诊登记<br>
     * - 索引：(pet, createTime)
     */
    default MPLambdaQuery<FirstRegistration> selectByPet(Long petId) {
        return lambdaQuery().eq(FirstRegistration::getPetId, petId);
    }

    /**
     * 查询初诊登记，包括宠物 id，宠物名，登记用户等<br>
     * - 索引：(petId, createTime)<br>
     * - 索引：(registrarId, createTime)
     */
    default MPLambdaQuery<FirstRegistration> selectByRequest(FirstRegistrationQueryParams params) {
        return selectByRequest(params, null);
    }

    default MPLambdaQuery<FirstRegistration> selectByRequest(FirstRegistrationQueryParams params, Set<Long> petIds) {
        return lambdaQuery()
                .eq(FirstRegistration::getPetId, params.getPet())
                .in(FirstRegistration::getPetId, petIds)
                .eq(FirstRegistration::getRegistrarId, params.getRegistrar())
                .in(FirstRegistration::getCreateTime, params.getDate0(), params.getDate1())
                .like(FirstRegistration::getName, params.getName());
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.first_registration";
    }

    @Override
    default Class<FirstRegistration> getEntityClass() {
        return FirstRegistration.class;
    }
}

