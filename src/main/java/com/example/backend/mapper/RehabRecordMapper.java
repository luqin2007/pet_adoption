package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.RehabRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (planId, createTime)
 */
@Mapper
public interface RehabRecordMapper extends IBaseMapper<RehabRecord> {

    default LambdaQueryWrapper<RehabRecord> queryByPlan(Long planId) {
        return new LambdaQueryWrapper<RehabRecord>()
            .eq(RehabRecord::getPlanId, planId)
            .orderByDesc(RehabRecord::getCreateTime);
    }
}
