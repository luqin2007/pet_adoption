package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.entity.ExaminationDiagnosis;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (detailId)
 */
@Mapper
public interface ExaminationDiagnosisMapper extends IBaseMapper<ExaminationDiagnosis> {

    default LambdaQueryWrapper<ExaminationDiagnosis> queryByDetail(Long detailId) {
        return lambdaQuery().eq(ExaminationDiagnosis::getDetailId, detailId);
    }

    default LambdaUpdateWrapper<ExaminationDiagnosis> discardById(Long diagnosisId) {
        return lambdaUpdate().eq(ExaminationDiagnosis::getId, diagnosisId).set(ExaminationDiagnosis::getIsDiscard, true);
    }

    @Override
    default String getMissingMessage() {
        return "诊断记录不存在";
    }
}
