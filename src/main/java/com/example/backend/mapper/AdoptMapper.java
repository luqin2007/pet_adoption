package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.dto.AdoptQueryParams;
import com.example.backend.entity.Adopt;
import com.example.backend.entity.property.AdoptBreadingStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * 索引：
 * - (petId, status, createTime)
 * - (applicantId, status, createTime)
 */
@Mapper
public interface AdoptMapper extends IBaseMapper<Adopt> {

    default LambdaQueryWrapper<Adopt> queryByRequest(AdoptQueryParams params) {
        LambdaQueryWrapper<Adopt> query = lambdaQuery();
        params.querySet(query, Adopt::getPetId, params.getPet())
                .querySet(query, Adopt::getApplicantId, params.getUser())
                .querySet(query, Adopt::getStatus, AdoptBreadingStatus::get, params.getStatus())
                .queryTime(query, Adopt::getCreateTime, params.getTime0(), params.getTime1());
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
