package com.example.backend.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StockEventListener {

    @Async
    @EventListener(StockEvent.class)
    public void stockChange(StockEvent event) {
        // TODO 通知
    }
}
