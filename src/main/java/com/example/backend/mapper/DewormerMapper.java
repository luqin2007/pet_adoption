package com.example.backend.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DewormerMapper extends IBaseMapper<Dewormer> {

    @Override
    default String getMissingMessage() {
        return "驱虫药信息不存在";
    }
}
