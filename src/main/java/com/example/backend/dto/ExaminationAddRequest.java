package com.example.backend.dto;

import com.example.backend.entity.Examination;
import com.example.backend.entity.property.ExamType;
import com.example.backend.entity.property.TextType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class ExaminationAddRequest {

    @NotBlank
    private String name;

    private String text;

    @NotBlank(message = "请选择文本类型")
    private String textType;

    @NotBlank(message = "请选择检查类型")
     private String examType;

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
                TextType.get(textType),
                ExamType.get(examType),
                filename,
                price,
                checkTime,
                new Date());
    }
}
