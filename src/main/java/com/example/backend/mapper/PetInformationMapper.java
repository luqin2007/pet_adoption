package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.PetInformation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PetInformationMapper extends BaseMapper<PetInformation> {

    @Select("select id from pet_information")
    List<Long> getAllIds();
}
