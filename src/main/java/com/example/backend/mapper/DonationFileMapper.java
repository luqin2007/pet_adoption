package com.example.backend.mapper;

import com.example.backend.entity.DonationFile;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (donationId)
 */
@Mapper
public interface DonationFileMapper extends IBaseMapper<DonationFile> {

    default MPLambdaQuery<DonationFile> queryByDonation(Long donationId) {
        return lambdaQuery().eq(DonationFile::getDonationId, donationId);
    }

    default MPLambdaQuery<DonationFile> queryByDonations(Set<Long> donationIds) {
        return lambdaQuery().in(DonationFile::getDonationId, donationIds);
    }

    @Override
    default String getMissingMessage() {
        return "捐赠文件不存在";
    }

    @Override
    default Class<DonationFile> getEntityClass() {
        return DonationFile.class;
    }
}
