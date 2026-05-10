package com.example.backend.mapper;

import com.example.backend.entity.DonationItem;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (donationId)
 */
@Mapper
public interface DonationItemMapper extends IBaseMapper<DonationItem> {

    default MPLambdaQuery<DonationItem> queryByDonation(Long donationId) {
        return lambdaQuery().eq(DonationItem::getDonationId, donationId);
    }

    default MPLambdaQuery<DonationItem> queryByDonations(Set<Long> donationIds) {
        return lambdaQuery().in(DonationItem::getDonationId, donationIds);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.donation_item";
    }

    @Override
    default Class<DonationItem> getEntityClass() {
        return DonationItem.class;
    }
}

