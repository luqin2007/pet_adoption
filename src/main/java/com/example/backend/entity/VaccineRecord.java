package com.example.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 疫苗接种记录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VaccineRecord implements IId {

    /**
     * *主键 bigint*
     */
    private Long id;

    /**
     * 宠物 id
     * *外键:pet(id) 非空 bigint*
     */
    private Long petId;

    /**
     * 宠物年龄（月）
     * *非空 int*
     */
    private Integer petAge;

    /**
     * 医生 id
     * *外键:user(id) 非空 bigint*
     */
    private Long doctorId;

    /**
     * 疫苗 id
     * *外键:vaccine(id) 非空 bigint*
     */
    private Long vaccineId;

    /**
     * 针次
     * *非空 int*
     */
    private Integer times;

    /**
     * 创建时间
     * *非空 Date*
     */
    private Date createTime;
}
