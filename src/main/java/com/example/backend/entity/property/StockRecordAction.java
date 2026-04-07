package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

public enum StockRecordAction {
    IN, // 入库
    OUT, // 出库
    DESTROY; // 销毁

    public boolean mayCreateStock() {
        return this == IN;
    }

    public static StockRecordAction get(String name) {
        try {
            return StockRecordAction.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("无效物资记录动作 " + name);
        }
    }
}
