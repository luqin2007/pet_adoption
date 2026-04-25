package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 宠物类型与品种信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfoPetType implements IId {

    private Long id;
    private String type;
    private String breed;
    private Date createTime;
}
