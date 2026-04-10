package com.example.backend.event;

import com.example.backend.entity.Stock;
import com.example.backend.entity.StockRecord;

/**
 * 入库/出库/销毁物品
 */
public record StockEvent(Stock stock, StockRecord record) {
}
