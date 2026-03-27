package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.ExaminationFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：<br>
 * - (examinationId)
 */
@Mapper
public interface ExaminationFileMapper extends IBaseMapper<ExaminationFile> {

    /**
     * 查询指定检查的附件<br>
     * 索引：(examinationId)
     */
    default LambdaQueryWrapper<ExaminationFile> queryByExamination(Long examinationId) {
        return lambdaQuery().eq(ExaminationFile::getExaminationId, examinationId);
    }

    /**
     * 批量查询指定检查的附件<br>
     * 索引：(examinationId)
     */
    default LambdaQueryWrapper<ExaminationFile> queryByExaminations(Set<Long> examinationIds) {
        return lambdaQuery().in(ExaminationFile::getExaminationId, examinationIds);
    }

    @Override
    default String getMissingMessage() {
        return "附件不存在";
    }
}
