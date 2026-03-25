package com.example.backend.dto;

import com.example.backend.entity.MedicalDetail;
import lombok.Data;

@Data
public class MedicalDetailUpdateRequest {

    private Long doctorId;
    private Boolean isCompleted;
    private String summary;

    private String physicalExam;

    private String diagnosis;
    private String differential;

    private String exam;
    private String treatment;
    private String advice;

    public void applyTo(MedicalDetail detail) {
        detail.setDoctorId(doctorId);
        detail.setIsCompleted(isCompleted);
        detail.setSummary(summary);
        detail.setPhysicalExam(physicalExam);
        detail.setDiagnosis(diagnosis);
        detail.setDifferential(differential);
        detail.setExam(exam);
        detail.setTreatment(treatment);
        detail.setAdvice(advice);
    }
}
