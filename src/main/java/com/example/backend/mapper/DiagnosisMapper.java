package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.entity.Diagnosis;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：<br>
 * - (detailId)
 */
@Mapper
public interface DiagnosisMapper extends IBaseMapper<Diagnosis> {

    /**
     * 根据病历 id 获取诊断结果<br>
     * - 索引：(detailId)
     */
    default LambdaQueryWrapper<Diagnosis> queryByDetail(Long detailId) {
        return lambdaQuery().eq(Diagnosis::getDetailId, detailId);
    }

    /**
     * 根据病历 id 获取诊断结果<br>
     * - 索引：(detailId)
     */
    default LambdaQueryWrapper<Diagnosis> queryByDetails(Set<Long> detailIds) {
        return lambdaQuery().in(Diagnosis::getDetailId, detailIds);
    }

    /**
     * 废弃诊断结果
     */
    default LambdaUpdateWrapper<Diagnosis> discardById(Long diagnosisId) {
        return lambdaUpdate().eq(Diagnosis::getId, diagnosisId).set(Diagnosis::getIsDiscard, true);
    }

    @Override
    default String getMissingMessage() {
        return "诊断记录不存在";
    }
}
