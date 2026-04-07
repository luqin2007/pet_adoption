package com.example.backend.dto;

import com.example.backend.entity.Stock;
import com.example.backend.entity.property.SourceType;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Set;

/**
 * 库存查询参数
 */
@Data
public class StockQueryParams implements IParam<Stock>, IRequestValidate {

    private Set<Long> item;
    private Set<Long> user;
    private Set<String> source;
    private Date time0;
    private Date time1;
    private Integer count;

    public Integer getCount(int defaultValue) {
        return count == null ? defaultValue : count;
    }

    @Override
    public void validate(Errors errors) {
        validateEnums(errors, StockQueryParams::getSource, SourceType.class, "request.item_donation.stock.source_type");
        validateTime(errors, StockQueryParams::getTime0, StockQueryParams::getTime1);
    }
}
