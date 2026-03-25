package com.example.backend.dto;

import com.example.backend.entity.AllergyHistory;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

@Data
public class AllergyHistoryAddRequest {

    @NotBlank(message = "请输入过敏源")
    private String source;

    @NotBlank(message = "发现时间")
    private Date discoveryTime;

    public AllergyHistory build(Long registrationId) {
        return new AllergyHistory(null,
                registrationId,
                source,
                discoveryTime,
                new Date());
    }
}
