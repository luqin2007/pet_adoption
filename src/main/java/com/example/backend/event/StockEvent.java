package com.example.backend.event;

import com.example.backend.entity.Stock;
import com.example.backend.entity.StockRecord;
import com.example.backend.util.LangHelper;

/**
 * 入库/出库/销毁物品
 * - 按库存订阅规则发送站内信：同步物资库存变动
 * - 按库存订阅规则发送邮件：同步库存阈值告警等重要信息
 *
 * @see com.example.backend.service.ItemDonationService#addStockRecord(com.example.backend.dto.StockRequest)
 */
public record StockEvent(Stock stock, StockRecord record) {

    public String buildStockChangeNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.stock_change.title");
    }

    public String buildStockChangeNotifyContent(LangHelper langHelper, String itemName) {
        return langHelper.get("notification.stock_change.content", itemName, record.getAction().name, record.getCount());
    }

    public String buildStockLowItemNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.item_low_stock.title");
    }

    public String buildStockLowItemNotifyContent(LangHelper langHelper, String itemName) {
        return langHelper.get("notification.item_low_stock.content", itemName);
    }

    public String buildDonationNotifyTitle(LangHelper langHelper) {
        return langHelper.get("notification.donation_stock.title");
    }

    public String buildDonationNotifyContent(LangHelper langHelper, String itemName) {
        return langHelper.get("notification.donation_stock.content", itemName, record.getAction().name);
    }

    public String buildStockLowItemMailTitle(LangHelper langHelper) {
        return langHelper.get("mail.item_low_stock.title");
    }

    public String buildStockLowItemMailContent(LangHelper langHelper, String itemName) {
        return langHelper.get("mail.item_low_stock.content", itemName);
    }

    public String buildDonationMailTitle(LangHelper langHelper) {
        return langHelper.get("notification.donation_stock.title");
    }

    public String buildDonationMailContent(LangHelper langHelper, String itemName) {
        return langHelper.get("notification.donation_stock.content", itemName, record.getAction().name);
    }
}
