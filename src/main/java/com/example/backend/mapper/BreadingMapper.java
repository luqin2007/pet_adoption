package com.example.backend.mapper;

import com.example.backend.dto.BreadingQueryParams;
import com.example.backend.entity.Breading;
import com.example.backend.entity.property.AdoptBreadingStatus;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * 索引：
 * - (applicantId, petType, status, createTime)
 * - (reviewerId, petType, status, createTime)
 */
@Mapper
public interface BreadingMapper extends IBaseMapper<Breading> {

    default MPLambdaQuery<Breading> queryByRequest(BreadingQueryParams params) {
        return lambdaQuery()
                .in(Breading::getApplicantId, params.getApplicant())
                .in(Breading::getReviewerId, params.getReviewer())
                .in(Breading::getPetType, params.getPetType())
                .in(Breading::getStatus, AdoptBreadingStatus::get, params.getStatus())
                .in(Breading::getCreateTime, params.getTime0(), params.getTime1());
    }

    default MPLambdaUpdate<Breading> updateStatus(Long id, AdoptBreadingStatus status) {
        Date now = new Date();
        return lambdaUpdate()
                .eq(Breading::getId, id)
                .set(Breading::getStatus, status)
                .set(Breading::getUpdateTime, now);
    }

    @Override
    default String getMissingMessage() {
        return "寄养申请不存在";
    }

    @Override
    default Class<Breading> getEntityClass() {
        return Breading.class;
    }
}
