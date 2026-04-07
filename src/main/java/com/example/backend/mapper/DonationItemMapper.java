package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.DonationItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (donationId)
 */
@Mapper
public interface DonationItemMapper extends IBaseMapper<DonationItem> {

    default LambdaQueryWrapper<DonationItem> queryByDonation(Long donationId) {
        return lambdaQuery().eq(DonationItem::getDonationId, donationId);
    }

    default LambdaQueryWrapper<DonationItem> queryByDonations(Set<Long> donationIds) {
        return lambdaQuery().in(DonationItem::getDonationId, donationIds);
    }
}
