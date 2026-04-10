package com.example.backend.mapper;

import com.example.backend.dto.StockRecordQueryParams;
import com.example.backend.entity.StockRecord;
import com.example.backend.entity.property.SourceType;
import com.example.backend.entity.property.StockAction;
import com.example.backend.util.MPLambdaQuery;
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

    default MPLambdaQuery<StockRecord> queryByRequest(StockRecordQueryParams params) {
        return lambdaQuery()
                .in(StockRecord::getStockId, params.getStock())
                .in(StockRecord::getUserId, params.getUser())
                .in(StockRecord::getAction, StockAction::get, params.getAction())
                .in(StockRecord::getSourceType, SourceType::get, params.getSource())
                .in(StockRecord::getCount, params.getCount0(), params.getCount1())
                .in(StockRecord::getRemain, params.getRemain0(), params.getRemain1())
                .in(StockRecord::getPrice, params.getPrice0(), params.getPrice1())
                .in(StockRecord::getTotalPrice, params.getTotal0(), params.getTotal1())
                .in(StockRecord::getCreateTime, params.getTime0(), params.getTime1())
                .like(StockRecord::getPurpose, params.getPurpose());
    }

    /**
     * 索引：
     * - (stockId, createTime)
     */
    default MPLambdaQuery<StockRecord> queryByStock(Long stockId, int count) {
        return lambdaQuery()
                .eq(StockRecord::getStockId, stockId)
                .asc(StockRecord::getCreateTime)
                .limit(count);
    }

    /**
     * 索引：
     * - (stockId, createTime)
     * TODO 检查数据库字段命名
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

    @Override
    default Class<StockRecord> getEntityClass() {
        return StockRecord.class;
    }
}
