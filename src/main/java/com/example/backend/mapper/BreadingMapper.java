package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.dto.BreadingQueryParams;
import com.example.backend.entity.Breading;
import com.example.backend.entity.property.AdoptBreadingStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * 索引：
 * - (applicantId, petType, status, createTime)
 * - (reviewerId, petType, status, createTime)
 */
@Mapper
public interface BreadingMapper extends IBaseMapper<Breading> {

    default LambdaQueryWrapper<Breading> queryByRequest(BreadingQueryParams params) {
        LambdaQueryWrapper<Breading> query = lambdaQuery();
        params.querySet(query, Breading::getApplicantId, params.getApplicant())
                .querySet(query, Breading::getReviewerId, params.getReviewer())
                .querySet(query, Breading::getPetType, params.getPetType())
                .querySet(query, Breading::getStatus, AdoptBreadingStatus::get, params.getStatus())
                .queryTime(query, Breading::getCreateTime, params.getTime0(), params.getTime1());
        return query;
    }

    default LambdaUpdateWrapper<Breading> updateStatus(Long id, AdoptBreadingStatus status) {
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
}
