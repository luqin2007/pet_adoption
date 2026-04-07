package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.backend.dto.FirstRegistrationQueryParams;
import com.example.backend.entity.FirstRegistration;
import com.example.backend.util.StringUtils;
import org.apache.ibatis.annotations.Mapper;

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
    default LambdaQueryWrapper<FirstRegistration> selectByPet(Long petId) {
        return lambdaQuery().eq(FirstRegistration::getPetId, petId);
    }

    /**
     * 查询初诊登记，包括宠物 id，宠物名，登记用户等<br>
     * - 索引：(petId, createTime)<br>
     * - 索引：(registrarId, createTime)
     */
    default LambdaQueryWrapper<FirstRegistration> selectByRequest(FirstRegistrationQueryParams params) {
        LambdaQueryWrapper<FirstRegistration> query = Wrappers.lambdaQuery();
        params.query(query, FirstRegistration::getPetId, params.getPet())
                .query(query, FirstRegistration::getRegistrarId, params.getRegistrar())
                .queryTime(query, FirstRegistration::getCreateTime, params.getDate0(), params.getDate1());
        query.like(StringUtils.hasText(params.getName()), FirstRegistration::getName, params.getName());
        return query;
    }

    @Override
    default String getMissingMessage() {
        return "初诊信息不存在";
    }
}
