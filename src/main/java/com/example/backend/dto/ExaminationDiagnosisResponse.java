package com.example.backend.dto;

import com.example.backend.entity.ExaminationDiagnosis;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ExaminationDiagnosisResponse {

    private Long id;
    private Long medicalId;
    private String result;
    private Date createTime;
    private List<ExaminationResponse> examinations;

    public static ExaminationDiagnosisResponse create(ExaminationDiagnosis diagnosis, List<ExaminationResponse> examinations) {
        return new ExaminationDiagnosisResponse(
                diagnosis.getId(),
                diagnosis.getDetailId(),
                diagnosis.getResult(),
                diagnosis.getCreateTime(),
                examinations);
    }

    /**
     * examinations: ExaminationDiagnosis.id
     */
    public static ExaminationDiagnosisResponse createBatch(ExaminationDiagnosis diagnosis, Map<Long, List<ExaminationResponse>> examinations) {
        return new ExaminationDiagnosisResponse(
                diagnosis.getId(),
                diagnosis.getDetailId(),
                diagnosis.getResult(),
                diagnosis.getCreateTime(),
                examinations.get(diagnosis.getId()));
    }
}
