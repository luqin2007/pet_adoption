package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.Examination;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (detailId)
 */
@Mapper
public interface ExaminationMapper extends IBaseMapper<Examination> {

    default LambdaQueryWrapper<Examination> queryByDetail(Long detailId) {
        return lambdaQuery().eq(Examination::getDetailId, detailId);
    }

    @Override
    default String getMissingMessage() {
        return "检查不存在";
    }
}
