package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.dto.StockQueryParams;
import com.example.backend.entity.Stock;
import com.example.backend.entity.property.SourceType;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

/**
 * 索引：
 * - (itemId, sourceType, createTime)
 * - (userId, sourceType, createTime)
 * - (sourceType, createTime)
 */
@Mapper
public interface StockMapper extends IBaseMapper<Stock> {

    default LambdaQueryWrapper<Stock> queryByRequest(StockQueryParams params) {
        LambdaQueryWrapper<Stock> query = lambdaQuery();
        params.querySet(query, Stock::getItemId, params.getItem())
                .querySet(query, Stock::getUserId, params.getUser())
                .querySet(query, Stock::getSourceType, SourceType::get, params.getSource())
                .queryTime(query, Stock::getCreateTime, params.getTime0(), params.getTime1());
        return query;
    }

    default LambdaQueryWrapper<Stock> queryByItem(Long itemId) {
        return lambdaQuery().eq(Stock::getItemId, itemId);
    }

    default LambdaQueryWrapper<Stock> queryByItems(Set<Long> itemIds) {
        return lambdaQuery().in(Stock::getItemId, itemIds);
    }

    default LambdaUpdateWrapper<Stock> updateCount(Long stockId, BigDecimal count) {
        return lambdaUpdate()
                .eq(Stock::getId, stockId)
                .set(Stock::getCount, count)
                .set(Stock::getUpdateTime, new Date());
    }

    @Override
    default String getMissingMessage() {
        return "库存记录不存在";
    }
}
