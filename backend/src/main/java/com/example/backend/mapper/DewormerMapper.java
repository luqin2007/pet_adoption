package com.example.backend.mapper;

import com.example.backend.entity.Dewormer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DewormerMapper extends IBaseMapper<Dewormer> {

    @Override
    default String getMissingMessage() {
        return "exception.not_found.dewormer";
    }

    @Override
    default Class<Dewormer> getEntityClass() {
        return Dewormer.class;
    }
}

