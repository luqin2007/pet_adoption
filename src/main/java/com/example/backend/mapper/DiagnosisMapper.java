package com.example.backend.mapper;

import com.example.backend.entity.Diagnosis;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
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
    default MPLambdaQuery<Diagnosis> queryByDetail(Long detailId) {
        return lambdaQuery().eq(Diagnosis::getDetailId, detailId);
    }

    /**
     * 根据病历 id 获取诊断结果<br>
     * - 索引：(detailId)
     */
    default MPLambdaQuery<Diagnosis> queryByDetails(Set<Long> detailIds) {
        return lambdaQuery().in(Diagnosis::getDetailId, detailIds);
    }

    /**
     * 废弃诊断结果
     */
    default MPLambdaUpdate<Diagnosis> discardById(Long diagnosisId) {
        return lambdaUpdate().eq(Diagnosis::getId, diagnosisId).set(Diagnosis::getIsDiscard, true);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.diagnosis";
    }

    @Override
    default Class<Diagnosis> getEntityClass() {
        return Diagnosis.class;
    }
}

