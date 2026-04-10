package com.example.backend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class LostPetMediaUploadTable implements ITable {

    private String name;

    private MultipartFile file;
}
