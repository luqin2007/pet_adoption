package com.example.backend.mapper;

import com.example.backend.entity.InfoPetType;
import com.example.backend.util.MPLambdaQuery;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InfoPetTypeMapper extends IBaseMapper<InfoPetType> {

    default MPLambdaQuery<InfoPetType> queryByTypeAndBreed(String type, String breed) {
        return lambdaQuery()
                .eq(InfoPetType::getType, type)
                .eq(InfoPetType::getBreed, breed);
    }

    default MPLambdaQuery<InfoPetType> queryAll() {
        return lambdaQuery()
                .asc(InfoPetType::getType)
                .asc(InfoPetType::getBreed);
    }

    @Override
    default Class<InfoPetType> getEntityClass() {
        return InfoPetType.class;
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.info_pet_type";
    }
}
