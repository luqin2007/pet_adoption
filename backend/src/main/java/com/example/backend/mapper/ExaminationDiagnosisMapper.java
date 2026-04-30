package com.example.backend.mapper;

import com.example.backend.entity.ExaminationDiagnosis;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：<br>
 * - (examinationId)
 */
@Mapper
public interface ExaminationDiagnosisMapper extends IBaseMapper<ExaminationDiagnosis> {

    /**
     * 获取检验结果关联的所有诊断结果<br>
     * - 索引：(examinationId)
     */
    default MPLambdaQuery<ExaminationDiagnosis> queryByExaminations(Set<Long> examinationIds) {
        return lambdaQuery().in(ExaminationDiagnosis::getExaminationId, examinationIds);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.examination_diagnosis";
    }

    @Override
    default Class<ExaminationDiagnosis> getEntityClass() {
        return ExaminationDiagnosis.class;
    }
}

