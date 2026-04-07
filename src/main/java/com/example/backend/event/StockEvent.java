package com.example.backend.event;

import com.example.backend.entity.StockRecord;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockEvent {

    private StockRecord record;
}
