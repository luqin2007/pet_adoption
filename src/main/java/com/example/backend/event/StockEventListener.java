package com.example.backend.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockEventListener {

    @Async
    @EventListener(StockEvent.InStockEvent.class)
    public void onInStock(StockEvent.InStockEvent event) {

    }

    @Async
    @EventListener(StockEvent.OutStockEvent.class)
    public void onOutStock(StockEvent.OutStockEvent event) {

    }
}
