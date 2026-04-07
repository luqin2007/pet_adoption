package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.DonationFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (donationId)
 */
@Mapper
public interface DonationFileMapper extends IBaseMapper<DonationFile> {

    default LambdaQueryWrapper<DonationFile> queryByDonation(Long donationId) {
        return lambdaQuery().eq(DonationFile::getDonationId, donationId);
    }

    default LambdaQueryWrapper<DonationFile> queryByDonations(Set<Long> donationIds) {
        return lambdaQuery().in(DonationFile::getDonationId, donationIds);
    }
}
