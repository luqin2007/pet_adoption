package com.example.backend.dto;

import com.example.backend.entity.Examination;
import com.example.backend.entity.property.ExamType;
import com.example.backend.entity.property.TextType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.validation.Errors;

import java.util.Date;

@Data
public class ExaminationAddRequest implements IRequest, IValidatedRequest {

    @NotBlank(message = "request.medical.exam.name")
    private String name;

    private String text;

    @NotBlank(message = "request.medical.exam.text_type")
    private String textType;

    @NotBlank(message = "request.medical.exam.exam_type")
    private String examType;

    private String filename;

    @NotNull(message = "request.medical.exam.time")
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
                checkTime,
                new Date());
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, ExaminationAddRequest::getTextType, TextType.class, "request.medical.exam.text_type");
        validateEnum(errors, ExaminationAddRequest::getExamType, ExamType.class, "request.medical.exam.exam_type");
        if (text == null && filename == null) // 检查结果
            errors.rejectValue("text", "request.medical.exam.content");
    }
}
