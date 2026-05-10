package com.example.backend.mapper;

import com.example.backend.entity.ExaminationFile;
import com.example.backend.util.MPLambdaQuery;
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
    default MPLambdaQuery<ExaminationFile> queryByExamination(Long examinationId) {
        return lambdaQuery().eq(ExaminationFile::getExaminationId, examinationId);
    }

    /**
     * 批量查询指定检查的附件<br>
     * 索引：(examinationId)
     */
    default MPLambdaQuery<ExaminationFile> queryByExaminations(Set<Long> examinationIds) {
        return lambdaQuery().in(ExaminationFile::getExaminationId, examinationIds);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.examination_file";
    }

    @Override
    default Class<ExaminationFile> getEntityClass() {
        return ExaminationFile.class;
    }
}

