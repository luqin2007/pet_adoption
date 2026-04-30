package com.example.backend.mapper;

import com.example.backend.dto.DonationQueryParams;
import com.example.backend.entity.Donation;
import com.example.backend.entity.property.DonationStatus;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * 索引：
 * - (userId, createTime)
 * - (createTime)
 */
@Mapper
public interface DonationMapper extends IBaseMapper<Donation> {

    default MPLambdaUpdate<Donation> updateStatus(Long id, DonationStatus status) {
        return lambdaUpdate()
                .eq(Donation::getId, id)
                .set(Donation::getStatus, status)
                .set(Donation::getUpdateTime, new Date());
    }

    default MPLambdaQuery<Donation> queryByRequest(DonationQueryParams params) {
        return lambdaQuery()
                .in(Donation::getUserId, params.getUser())
                .in(Donation::getCreateTime, params.getDate0(), params.getDate1());
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.donation";
    }

    @Override
    default Class<Donation> getEntityClass() {
        return Donation.class;
    }
}

