package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.backend.dto.FirstRegistrationQueryParams;
import com.example.backend.entity.FirstRegistration;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.Objects;
import java.util.stream.Stream;

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
        // 检查参数
        Long registrar = params.getRegistrar();
        Long pet = params.getPet();
        String name = params.getName();
        Date date0 = params.getDate0();
        Date date1 = params.getDate1();
        long count = Stream.of(registrar, pet, name).filter(Objects::nonNull).count();
        require(count <= 1, "user, pet, name 不能同时查询"); // user, pet, name 互斥
        require(date0 == null || date1 == null || date0.before(date1), "date0 > date1"); // 日期顺序

        LambdaQueryWrapper<FirstRegistration> query = Wrappers.lambdaQuery();
        // 宠物 id
        query.eq(pet != null, FirstRegistration::getPetId, pet);
        // 检查用户 id
        query.eq(registrar != null, FirstRegistration::getRegistrarId, registrar);
        // 日期 索引：
        query.between(date0 != null && date1 != null, FirstRegistration::getCreateTime, date0, date1);
        query.ge(date0 != null && date1 == null, FirstRegistration::getCreateTime, date0);
        query.le(date0 == null && date1 != null, FirstRegistration::getCreateTime, date1);
        // 其他
        query.like(name != null, FirstRegistration::getName, name);

        return query;
    }

    @Override
    default String getMissingMessage() {
        return "初诊信息不存在";
    }
}
