package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.ExaminationDiagnosisEntry;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (examinationId)
 */
@Mapper
public interface ExaminationDiagnosisEntryMapper extends IBaseMapper<ExaminationDiagnosisEntry> {

    default LambdaQueryWrapper<ExaminationDiagnosisEntry> queryByExaminations(Set<Long> examinationIds) {
        return lambdaQuery().in(ExaminationDiagnosisEntry::getExaminationId, examinationIds);
    }

    @Override
    default String getMissingMessage() {
        return "诊断关系不存在";
    }
}
