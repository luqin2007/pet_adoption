package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.RehabRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：<br>
 * - (planId, createTime)
 */
@Mapper
public interface RehabRecordMapper extends IBaseMapper<RehabRecord> {

    /**
     * 根据康复计划获取康复记录（倒序）<br>
     * - 索引：(planId, createTime)
     */
    default LambdaQueryWrapper<RehabRecord> queryByPlan(Long planId) {
        return new LambdaQueryWrapper<RehabRecord>()
            .eq(RehabRecord::getPlanId, planId)
            .orderByDesc(RehabRecord::getCreateTime);
    }
}
