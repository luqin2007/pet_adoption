package com.example.backend.mapper;

import com.example.backend.entity.Vaccine;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VaccineMapper extends IBaseMapper<Vaccine> {

    @Override
    default String getMissingMessage() {
        return "找不到疫苗";
    }
}
