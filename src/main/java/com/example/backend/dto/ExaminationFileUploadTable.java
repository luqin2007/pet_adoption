package com.example.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ExaminationFileUploadTable {

    private String name;

    @NotNull
    private MultipartFile file;
}
