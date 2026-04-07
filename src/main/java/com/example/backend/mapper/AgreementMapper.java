package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.dto.AgreementQueryParams;
import com.example.backend.entity.Agreement;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * 索引：
 * - (parentType, parentId, signed, createTime)
 */
@Mapper
public interface AgreementMapper extends IBaseMapper<Agreement> {

    default LambdaQueryWrapper<Agreement> queryByRequest(AgreementQueryParams params) {
        LambdaQueryWrapper<Agreement> query = lambdaQuery();
        params.query(query, Agreement::getParentType, params.getParentType())
                .query(query, Agreement::getParentId, params.getParentId())
                .queryExist(query, Agreement::getSignTime, params.getSigned())
                .queryTime(query, Agreement::getCreateTime, params.getTime0(), params.getTime1());
        return query;
    }

    default LambdaUpdateWrapper<Agreement> setUpdateTime(Long agreementId) {
        return lambdaUpdate()
                .eq(Agreement::getId, agreementId)
                .set(Agreement::getUpdateTime, new Date());
    }

    @Override
    default String getMissingMessage() {
        return "协议不存在";
    }
}
