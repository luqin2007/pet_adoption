package com.example.backend.mapper;

import com.example.backend.dto.AdoptQueryParams;
import com.example.backend.entity.Adopt;
import com.example.backend.entity.property.AdoptBreadingStatus;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * 索引：
 * - (petId, status, createTime)
 * - (applicantId, status, createTime)
 */
@Mapper
public interface AdoptMapper extends IBaseMapper<Adopt> {

    default MPLambdaQuery<Adopt> queryByRequest(AdoptQueryParams params) {
        return lambdaQuery()
                .in(Adopt::getPetId, params.getPet())
                .in(Adopt::getApplicantId, params.getUser())
                .in(Adopt::getStatus, AdoptBreadingStatus::get, params.getStatus())
                .in(Adopt::getCreateTime, params.getTime0(), params.getTime1());
    }

    default MPLambdaUpdate<Adopt> updateStatus(Long id, AdoptBreadingStatus status) {
        Date now = new Date();
        return lambdaUpdate()
                .eq(Adopt::getId, id)
                .set(Adopt::getStatus, status)
                .set(status == AdoptBreadingStatus.AGREEMENT_SIGNED, Adopt::getAdoptTime, now)
                .set(Adopt::getUpdateTime, now);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.adopt";
    }

    @Override
    default Class<Adopt> getEntityClass() {
        return Adopt.class;
    }
}

