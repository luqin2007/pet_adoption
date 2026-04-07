package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.DonationStatusUpdateRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 外键：
 * - (donationId, createTime)
 */
@Mapper
public interface DonationStatusUpdateMapper extends IBaseMapper<DonationStatusUpdateRecord> {

    default LambdaQueryWrapper<DonationStatusUpdateRecord> queryByDonation(Long donationId) {
        return lambdaQuery()
                .eq(DonationStatusUpdateRecord::getDonationId, donationId)
                .orderByDesc(DonationStatusUpdateRecord::getCreateTime);
    }

    default LambdaQueryWrapper<DonationStatusUpdateRecord> queryByDonations(Set<Long> donationIds) {
        return lambdaQuery()
                .in(DonationStatusUpdateRecord::getDonationId, donationIds)
                .orderByDesc(DonationStatusUpdateRecord::getCreateTime);
    }
}
