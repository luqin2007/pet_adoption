package com.example.backend.dto;

import com.example.backend.entity.Examination;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.util.Date;

import static com.example.backend.util.C.*;
import static com.example.backend.util.C.EXAM_TYPE_MAX;

@Data
public class ExaminationAddRequest {

    @NotBlank
    private String name;

    private String text;

    @NotNull(message = "未知文本类型")
    @Range(min = TEXT_TYPE_MIN, max = TEXT_TYPE_MAX, message = "未知文本类型")
    private Integer textType;

    @NotNull(message = "未知检查类型")
    @Range(min = EXAM_TYPE_MIN, max = EXAM_TYPE_MAX, message = "未知检查类型")
    private Integer examType;

    private String filename;

    private Double price;

    @NotNull
    private Date checkTime;

    public Examination create(Long userId, Long detailId) {
        return new Examination(null,
                userId,
                detailId,
                name,
                text,
                textType,
                examType,
                filename,
                price,
                checkTime,
                new Date());
    }
}
