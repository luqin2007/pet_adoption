package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.dto.StockRecordQueryParams;
import com.example.backend.entity.StockRecord;
import com.example.backend.entity.property.SourceType;
import com.example.backend.entity.property.StockRecordAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * 物资流转记录表操作
 */
@Mapper
public interface StockRecordMapper extends IBaseMapper<StockRecord> {

    /**
     * 按库存批次、物资、来源类型和动作查询记录
     */
    default LambdaQueryWrapper<StockRecord> queryByRequest(StockRecordQueryParams params) {
        LambdaQueryWrapper<StockRecord> query = lambdaQuery();
        //noinspection unchecked
        params.querySet(query, StockRecord::getStockId, params.getStock())
                .querySet(query, StockRecord::getUserId, params.getUser())
                .querySet(query, StockRecord::getAction, StockRecordAction::get, params.getAction())
                .querySet(query, StockRecord::getSourceType, SourceType::get, params.getSource())
                .queryDecimal(query, StockRecord::getCount, params.getCount0(), params.getCount1())
                .queryDecimal(query, StockRecord::getRemainCount, params.getRemain0(), params.getRemain1())
                .queryDecimal(query, StockRecord::getPrice, params.getPrice0(), params.getPrice1())
                .queryDecimal(query, StockRecord::getTotalPrice, params.getTotal0(), params.getTotal1())
                .queryTime(query, StockRecord::getCreateTime, params.getTime0(), params.getTime1())
                .queryText(query, StockRecord::getPurpose, params.getPurpose());
        return query;
    }

    /**
     * 索引：
     * - (stockId)
     */
    default LambdaQueryWrapper<StockRecord> queryByStock(Long stockId) {
        return lambdaQuery()
                .eq(StockRecord::getStockId, stockId);
    }

    /**
     * 索引：
     * - (stockId, createTime)
     */
    default LambdaQueryWrapper<StockRecord> queryByStock(Long stockId, int count) {
        return lambdaQuery()
                .eq(StockRecord::getStockId, stockId)
                .orderByAsc(StockRecord::getCreateTime)
                .last("LIMIT " + count);
    }

    /**
     * 索引：
     * - (stockId, createTime)
     */
    /* 窗口函数 ？
    SELECT * FROM (
        SELECT *,
               ROW_NUMBER() OVER (PARTITION BY stock_id ORDER BY create_time ASC) as rn
        FROM stock_record
        WHERE stock_id IN (...)
    ) t
    WHERE t.rn <= #{count}
     */
    @Select("select * from (" +
            "select *, ROW_NUMBER() OVER (PARTITION BY stock_id ORDER BY create_time ASC) as rn " +
            "from stock_record " +
            "where stock_id in " +
            "    <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            ") t where t.rn <= #{count}")
    List<StockRecord> queryByStocks(@Param("ids") Set<Long> stockIds, @Param("count") Integer count);

    @Override
    default String getMissingMessage() {
        return "物资使用记录不存在";
    }
}
