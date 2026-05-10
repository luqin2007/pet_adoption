package com.example.backend.entity.property;

import com.example.backend.util.ServiceException;

import java.util.Locale;

public enum StockAction {
    IN("入库"), // 入库
    OUT("出库"), // 出库
    DESTROY("销毁"); // 销毁

    public final String name;

    StockAction(String name) {
        this.name = name;
    }

    public boolean mayCreateStock() {
        return this == IN;
    }

    public static StockAction get(String name) {
        try {
            return StockAction.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ServiceException.invalidate("exception.invalidate.stock_action");
        }
    }
}
