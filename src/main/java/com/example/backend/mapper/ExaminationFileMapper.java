package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.ExaminationFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：
 * - (examinationId)
 */
@Mapper
public interface ExaminationFileMapper extends IBaseMapper<ExaminationFile> {

    default LambdaQueryWrapper<ExaminationFile> queryByExamination(Long examinationId) {
        return lambdaQuery().eq(ExaminationFile::getExaminationId, examinationId);
    }

    default LambdaQueryWrapper<ExaminationFile> queryByExaminations(Set<Long> examinationIds) {
        return lambdaQuery().in(ExaminationFile::getExaminationId, examinationIds);
    }

    @Override
    default String getMissingMessage() {
        return "检查文档不存在";
    }
}
