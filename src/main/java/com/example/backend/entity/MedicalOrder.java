package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 处方
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalOrder implements IId {

    /**
     * *主键 int*
     */
    private Long id;
}
