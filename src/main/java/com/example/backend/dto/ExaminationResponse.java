package com.example.backend.dto;

import com.example.backend.entity.Examination;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ExaminationResponse {

    private Long id;
    private Long detailId;
    private String text;
    private Integer textType;
    private Integer examType;
    private List<ExaminationFileResponse> files;
    private Date checkTime;
    private Date createTime;

    public static ExaminationResponse create(Examination examination, List<ExaminationFileResponse> files) {
        return new ExaminationResponse(
                examination.getId(),
                examination.getDetailId(),
                examination.getText(),
                examination.getTextType(),
                examination.getExamType(),
                files,
                examination.getCheckTime(),
                examination.getCreateTime());
    }

    /**
     * examinations: examinationId<br>
     * files: examinationId
     */
    public static ExaminationResponse createBatch(Long examinationId,
                                                  Map<Long, Examination> examinations,
                                                  Map<Long, List<ExaminationFileResponse>> files) {
        return create(examinations.get(examinationId), files.get(examinationId));
    }

    /**
     * files: Examination.id
     */
    public static ExaminationResponse createBatch(Examination examination, Map<Long, List<ExaminationFileResponse>> files) {
        return create(examination, files.get(examination.getId()));
    }
}
