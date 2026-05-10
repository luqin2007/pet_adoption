package com.example.backend.dto;

import com.example.backend.entity.RehabRecord;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Data
public class RehabRecordAddTable implements ITable {

    @NotBlank(message = "request.medical.rehab.record.step")
    private String step;

    @NotBlank(message = "request.medical.rehab.record.reaction")
    private String reaction;

    @NotBlank(message = "request.medical.rehab.record.note")
    private String note;

    private List<MultipartFile> files;

    public RehabRecord create(Long planId, Long doctorId) {
        return new RehabRecord(null,
                planId,
                doctorId,
                step,
                reaction,
                note,
                new Date());
    }
}
