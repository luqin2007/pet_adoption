package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.ExaminationDiagnosis;
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
    default LambdaQueryWrapper<ExaminationDiagnosis> queryByExaminations(Set<Long> examinationIds) {
        return lambdaQuery().in(ExaminationDiagnosis::getExaminationId, examinationIds);
    }

    @Override
    default String getMissingMessage() {
        return "诊断关系不存在";
    }
}
