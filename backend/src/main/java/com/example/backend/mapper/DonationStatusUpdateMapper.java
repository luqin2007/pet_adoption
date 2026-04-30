package com.example.backend.mapper;

import com.example.backend.entity.DonationStatusUpdateRecord;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 外键：
 * - (donationId, createTime)
 */
@Mapper
public interface DonationStatusUpdateMapper extends IBaseMapper<DonationStatusUpdateRecord> {

    default MPLambdaQuery<DonationStatusUpdateRecord> queryByDonation(Long donationId) {
        return lambdaQuery()
                .eq(DonationStatusUpdateRecord::getDonationId, donationId)
                .desc(DonationStatusUpdateRecord::getCreateTime);
    }

    default MPLambdaQuery<DonationStatusUpdateRecord> queryByDonations(Set<Long> donationIds) {
        return lambdaQuery()
                .in(DonationStatusUpdateRecord::getDonationId, donationIds)
                .desc(DonationStatusUpdateRecord::getCreateTime);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.donation_status_update";
    }

    @Override
    default Class<DonationStatusUpdateRecord> getEntityClass() {
        return DonationStatusUpdateRecord.class;
    }
}

