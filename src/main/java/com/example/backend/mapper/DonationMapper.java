package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.dto.DonationQueryParams;
import com.example.backend.entity.Donation;
import com.example.backend.entity.property.DonationStatus;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * 索引：
 * - (userId, createTime)
 * - (createTime)
 */
@Mapper
public interface DonationMapper extends IBaseMapper<Donation> {

    default LambdaUpdateWrapper<Donation> updateStatus(Long id, DonationStatus status) {
        return lambdaUpdate()
                .eq(Donation::getId, id)
                .set(Donation::getStatus, status)
                .set(Donation::getUpdateTime, new Date());
    }

    default LambdaQueryWrapper<Donation> queryByRequest(DonationQueryParams params) {
        LambdaQueryWrapper<Donation> query = lambdaQuery();
        params.querySet(query, Donation::getUserId, params.getUser())
                .queryTime(query, Donation::getCreateTime, params.getDate0(), params.getDate1());
        return query;
    }
}
