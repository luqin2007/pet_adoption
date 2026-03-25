package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DewormRecord implements IId {

    /**
     * *主键 int*
     */
    private Long id;

    /**
     * 宠物id
     * *外键:pet(id) 非空 int*
     */
    private Long petId;

    /**
     * 医生 id
     * *外键:user(id) 非空 int*
     */
    private Long doctorId;

    /**
     * 驱虫药 id
     * *外键:dewormer(id) 非空 int*
     */
    private Long dewormerId;

    /**
     * 次数
     * *非空 int*
     */
    private Integer times;

    /**
     * 创建时间
     * *非空 Date*
     */
    private Date createTime;
}
