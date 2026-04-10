package com.example.backend.mapper;

import com.example.backend.entity.Examination;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.Set;

/**
 * 索引：<br>
 * - (detailId)
 */
@Mapper
public interface ExaminationMapper extends IBaseMapper<Examination> {

    /**
     * 根据病历查询检查内容<br>
     * - 索引：(detailId)
     */
    default MPLambdaQuery<Examination> queryByDetail(Long detailId) {
        return lambdaQuery().eq(Examination::getDetailId, detailId);
    }

    /**
     * 根据病历查询检查内容<br>
     * - 索引：(detailId)
     */
    default MPLambdaQuery<Examination> queryByDetails(Set<Long> detailIds) {
        return lambdaQuery().in(Examination::getDetailId, detailIds);
    }

    @Override
    default String getMissingMessage() {
        return "检查不存在";
    }

    @Override
    default Class<Examination> getEntityClass() {
        return Examination.class;
    }
}
