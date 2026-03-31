package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.dto.AgreementQueryParams;
import com.example.backend.entity.Agreement;
import com.example.backend.entity.property.ParentType;
import com.example.backend.util.StringUtils;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * 索引：
 * - (parentType, parentId, signed, createTime)
 */
@Mapper
public interface AgreementMapper extends IBaseMapper<Agreement> {

    default LambdaQueryWrapper<Agreement> queryByParent(ParentType parentType, Long parentId) {
        return lambdaQuery()
                .eq(Agreement::getParentType, parentType)
                .eq(Agreement::getParentId, parentId);
    }

    default LambdaQueryWrapper<Agreement> queryByRequest(AgreementQueryParams params) {
        Long parentId = params.getParentId();
        String parentType = params.getParentType();
        Boolean signed = params.getSigned();
        Date time0 = params.getTime0();
        Date time1 = params.getTime1();
        require(time0 == null || time1 == null || time0.before(time1), "时间参数异常");
        // 查询 parentId 必须附带 parentType
        require(parentId == null || parentType != null, "参数异常");

        LambdaQueryWrapper<Agreement> query = lambdaQuery();
        if (StringUtils.hasText(parentType))
            query.eq(Agreement::getParentType, ParentType.get(parentType));
        query.eq(parentId != null, Agreement::getParentId, parentId);
        query.isNotNull(Boolean.TRUE.equals(signed), Agreement::getSignTime);
        query.isNull(Boolean.FALSE.equals(signed), Agreement::getSignTime);
        query.ge(time0 != null && time1 == null, Agreement::getCreateTime, time0);
        query.le(time0 == null && time1 != null, Agreement::getCreateTime, time1);
        query.in(time0 != null && time1 != null, Agreement::getCreateTime, time0, time1);
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
