package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.entity.Category;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

@Mapper
public interface CategoryMapper extends IBaseMapper<Category> {

    default LambdaUpdateWrapper<Category> discard(Long categoryId) {
        return new LambdaUpdateWrapper<Category>()
                .eq(Category::getId, categoryId)
                .set(Category::getIsDiscard, true)
                .set(Category::getUpdateTime, new Date());
    }

    default LambdaQueryWrapper<Category> queryName(String name) {
        return new LambdaQueryWrapper<Category>()
                .like(Category::getName, name)
                .eq(Category::getIsDiscard, false);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.category";
    }
}

