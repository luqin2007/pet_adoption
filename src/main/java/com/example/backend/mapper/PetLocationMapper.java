package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.PetLocation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetLocationMapper extends BaseMapper<PetLocation> {
}
