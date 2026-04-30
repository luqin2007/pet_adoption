package com.example.backend.dto;

import com.example.backend.entity.property.SourceType;
import com.example.backend.entity.property.StockAction;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

/**
 * 库存查询参数
 */
@Data
public class StockRecordQueryParams implements IParam, IValidatedRequest {

    private Set<Long> stock;
    private Set<Long> user;
    private Set<String> action;
    private Set<String> source;
    private String count0, count1;
    private String remain0, remain1;
    private String price0, price1;
    private String total0, total1;
    private Date time0, time1;
    private String purpose;

    @Override
    public void validate(Errors errors) {
        validateEnums(errors, StockRecordQueryParams::getAction, StockAction.class, "request.item_donation.stock.action");
        validateEnums(errors, StockRecordQueryParams::getSource, SourceType.class, "request.item_donation.stock.source_type");
        validateTime(errors, StockRecordQueryParams::getTime0, StockRecordQueryParams::getTime1);
        validateRange(errors, StockRecordQueryParams::getCount0, StockRecordQueryParams::getCount1, "request.item_donation.count");
        validateRange(errors, StockRecordQueryParams::getRemain0, StockRecordQueryParams::getRemain1, "request.item_donation.count");
        validateRange(errors, StockRecordQueryParams::getPrice0, StockRecordQueryParams::getPrice1, "request.item_donation.order.price");
        validateRange(errors, StockRecordQueryParams::getTotal0, StockRecordQueryParams::getTotal1, "request.item_donation.stock.total_price");
    }
}
