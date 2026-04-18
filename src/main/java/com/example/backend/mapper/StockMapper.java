package com.example.backend.mapper;

import com.example.backend.dto.StockQueryParams;
import com.example.backend.entity.Stock;
import com.example.backend.entity.property.SourceType;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 索引：
 * - (itemId, sourceType, createTime)
 * - (userId, sourceType, createTime)
 * - (sourceType, createTime)
 */
@Mapper
public interface StockMapper extends IBaseMapper<Stock> {

    default MPLambdaQuery<Stock> queryByRequest(StockQueryParams params) {
        return lambdaQuery()
                .in(Stock::getItemId, params.getItem())
                .in(Stock::getUserId, params.getUser())
                .in(Stock::getSourceType, SourceType::get, params.getSource())
                .in(Stock::getCreateTime, params.getTime0(), params.getTime1());
    }

    default MPLambdaQuery<Stock> queryByItem(Long itemId) {
        return lambdaQuery().eq(Stock::getItemId, itemId);
    }

    default MPLambdaUpdate<Stock> updateCount(Long stockId, BigDecimal count) {
        return lambdaUpdate()
                .eq(Stock::getId, stockId)
                .set(Stock::getCount, count)
                .set(Stock::getUpdateTime, new Date());
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.stock";
    }

    @Override
    default Class<Stock> getEntityClass() {
        return Stock.class;
    }
}

