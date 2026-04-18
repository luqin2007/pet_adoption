package com.example.backend.dto;

import com.example.backend.entity.property.NoticeSource;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.List;

@Data
public class NoticeQueryParams implements IParam, IValidatedRequest {

    /**
     * 已读
     */
    private Boolean read;

    /**
     * 查询分区
     */
    private List<String> source;

    /**
     * 时间范围
     */
    private Date time0, time1;


    @Override
    public void validate(Errors errors) {
        validateEnums(errors, NoticeQueryParams::getSource, NoticeSource.class, "exception.invalidate.source");
        validateTime(errors, NoticeQueryParams::getTime0, NoticeQueryParams::getTime1);
    }
}
