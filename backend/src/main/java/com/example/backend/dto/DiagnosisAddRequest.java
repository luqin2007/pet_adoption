package com.example.backend.dto;

import com.example.backend.entity.Diagnosis;
import com.example.backend.entity.ExaminationDiagnosis;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DiagnosisAddRequest implements IRequest {

    @NotBlank(message = "request.medical.diagnosis.result")
    private String result;

    @NotEmpty(message = "request.medical.diagnosis.exam")
    private List<Long> examinations;

    public Diagnosis create(Long detailId) {
        return new Diagnosis(null,
                detailId,
                result,
                new Date(),
                false);
    }

    public List<ExaminationDiagnosis> createEntries(Long diagnosisId) {
        Date now = new Date();
        return examinations.stream()
                .map(id -> new ExaminationDiagnosis(null, id, diagnosisId, now))
                .toList();
    }
}
