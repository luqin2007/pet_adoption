package com.example.backend.entity;

import com.example.backend.entity.property.ParentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetFeatureCache implements IId {

    private Long id;
    private Long parentId;
    private ParentType parentType;
    private String modelVersion;
    private String features;
    private Date scrapedAt;
    private Date createdAt;
    private Date updatedAt;
}
