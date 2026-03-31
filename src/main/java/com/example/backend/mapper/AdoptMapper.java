package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.dto.AdoptQueryParams;
import com.example.backend.entity.Adopt;
import com.example.backend.entity.property.AdoptBreadingStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 索引：
 * - (petId, status, createTime)
 * - (applicantId, status, createTime)
 */
@Mapper
public interface AdoptMapper extends IBaseMapper<Adopt> {

    default LambdaQueryWrapper<Adopt> queryByRequest(AdoptQueryParams params) {
        Set<Long> pets = params.getPet();
        Set<Long> applicants = params.getApplicant();
        Set<AdoptBreadingStatus> status = Optional.ofNullable(params.getStatus()).stream()
                .flatMap(Set::stream)
                .map(AdoptBreadingStatus::get)
                .collect(Collectors.toSet());
        Date time0 = params.getTime0();
        Date time1 = params.getTime1();
        require(time0 == null || time1 == null || time1.after(time0), "时间范围错误");

        LambdaQueryWrapper<Adopt> query = lambdaQuery();
        if (pets != null) {
            query.eq(pets.size() == 1, Adopt::getPetId, pets.iterator().next());
            query.in(pets.size() > 1, Adopt::getPetId, pets);
        }
        if (applicants != null) {
            query.eq(applicants.size() == 1, Adopt::getApplicantId, applicants.iterator().next());
            query.in(applicants.size() > 1, Adopt::getApplicantId, applicants);
        }
        query.eq(status.size() == 1, Adopt::getStatus, status.iterator().next());
        query.in(status.size() > 1, Adopt::getStatus, status);
        query.in(time0 != null && time1 != null, Adopt::getCreateTime, time0, time1);
        query.ge(time0 != null && time1 == null, Adopt::getCreateTime, time0);
        query.le(time0 == null && time1 != null, Adopt::getCreateTime, time1);
        return query;
    }

    default LambdaUpdateWrapper<Adopt> updateStatus(Long id, AdoptBreadingStatus status) {
        Date now = new Date();
        return lambdaUpdate()
                .eq(Adopt::getId, id)
                .set(Adopt::getStatus, status)
                .set(status == AdoptBreadingStatus.AGREEMENT_SIGNED, Adopt::getAdoptTime, now)
                .set(Adopt::getUpdateTime, now);
    }

    @Override
    default String getMissingMessage() {
        return "领养申请不存在";
    }
}
