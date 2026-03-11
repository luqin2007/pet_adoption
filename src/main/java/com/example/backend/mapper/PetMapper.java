package com.example.backend.mapper;

import com.example.backend.entity.Pet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PetMapper extends IBaseMapper<Pet> {
}
