package com.example.backend.dto;

import com.example.backend.entity.Examination;
import com.example.backend.entity.property.ExamType;
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

    @NotBlank(message = "request.medical.exam.exam_type")
    private String examType;

    @NotNull(message = "request.medical.exam.time")
    private Date checkTime;

    public Examination create(Long userId, Long detailId) {
        return new Examination(null,
                userId,
                detailId,
                name,
                text,
                ExamType.get(examType),
                checkTime,
                new Date());
    }

    @Override
    public void validate(Errors errors) {
        validateEnum(errors, ExaminationAddRequest::getExamType, ExamType.class, "request.medical.exam.exam_type");
    }
}
