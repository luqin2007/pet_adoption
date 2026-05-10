package com.example.backend.dto;

import com.example.backend.entity.MedicalDetail;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicalDetailUpdateRequest implements IRequest {

    @NotNull(message = "request.medical.detail.doctor")
    private Long doctorId;
    @NotNull(message = "request.medical.detail.complete")
    private String summary;

    private String physicalExam;

    private String diagnosis;
    private String differential;

    private String exam;
    private String treatment;
    private String advice;

    public void applyTo(MedicalDetail detail) {
        detail.setDoctorId(doctorId);
        detail.setSummary(summary);
        detail.setPhysicalExam(physicalExam);
        detail.setDiagnosis(diagnosis);
        detail.setDifferential(differential);
        detail.setExam(exam);
        detail.setTreatment(treatment);
        detail.setAdvice(advice);
    }
}
