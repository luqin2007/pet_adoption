package com.example.backend.mapper;

import com.example.backend.entity.Item;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemMapper extends IBaseMapper<Item> {

    @Override
    default String getMissingMessage() {
        return "物品不存在";
    }
}
