package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.dto.BreadingQueryParams;
import com.example.backend.entity.Breading;
import com.example.backend.entity.property.AdoptBreadingStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 索引：
 * - (applicantId, petType, status, createTime)
 * - (reviewerId, petType, status, createTime)
 */
@Mapper
public interface BreadingMapper extends IBaseMapper<Breading> {

    default LambdaQueryWrapper<Breading> queryByRequest(BreadingQueryParams params) {
        Set<Long> applicants = params.getApplicant();
        Set<Long> reviewers = params.getReviewer();
        Set<String> petTypes = params.getPetType();
        Set<AdoptBreadingStatus> status = Stream.ofNullable(params.getStatus())
                .flatMap(Set::stream)
                .map(AdoptBreadingStatus::get)
                .collect(Collectors.toSet());
        Date time0 = params.getTime0();
        Date time1 = params.getTime1();
        require(time0 == null || time1 == null || time0.before(time1), "时间范围错误");

        LambdaQueryWrapper<Breading> query = lambdaQuery();
        if (applicants != null) {
            query.eq(applicants.size() == 1, Breading::getApplicantId, applicants.iterator().next());
            query.in(applicants.size() > 1, Breading::getApplicantId, applicants);
        }
        if (reviewers != null) {
            query.eq(reviewers.size() == 1, Breading::getReviewerId, reviewers.iterator().next());
            query.in(reviewers.size() > 1, Breading::getReviewerId, reviewers);
        }
        if (petTypes != null) {
            query.eq(petTypes.size() == 1, Breading::getPetType, petTypes.iterator().next());
            query.in(petTypes.size() > 1, Breading::getPetType, petTypes);
        }
        query.eq(status.size() == 1, Breading::getStatus, status.iterator().next());
        query.in(status.size() > 1, Breading::getStatus, status);
        query.in(time0 != null && time1 != null, Breading::getCreateTime, time0, time1);
        query.ge(time0 != null && time1 == null, Breading::getCreateTime, time0);
        query.le(time0 == null && time1 != null, Breading::getCreateTime, time1);
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
