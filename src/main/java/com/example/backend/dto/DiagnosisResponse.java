package com.example.backend.dto;

import com.example.backend.entity.Diagnosis;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DiagnosisResponse implements IResponse {

    private Long id;
    private Long detailId;
    private String result;
    private Date createTime;
    private List<ExaminationResponse> examinations;

    public static DiagnosisResponse create(Diagnosis diagnosis, List<ExaminationResponse> examinations) {
        return new DiagnosisResponse(
                diagnosis.getId(),
                diagnosis.getDetailId(),
                diagnosis.getResult(),
                diagnosis.getCreateTime(),
                examinations);
    }

    /**
     * examinations: ExaminationDiagnosis.id
     */
    public static DiagnosisResponse createBatch(Diagnosis diagnosis, Map<Long, List<ExaminationResponse>> examinations) {
        return new DiagnosisResponse(
                diagnosis.getId(),
                diagnosis.getDetailId(),
                diagnosis.getResult(),
                diagnosis.getCreateTime(),
                examinations.get(diagnosis.getId()));
    }
}
