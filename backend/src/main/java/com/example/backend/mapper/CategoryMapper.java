package com.example.backend.mapper;

import com.example.backend.entity.Category;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

@Mapper
public interface CategoryMapper extends IBaseMapper<Category> {

    default MPLambdaUpdate<Category> discard(Long categoryId) {
        return lambdaUpdate()
                .eq(Category::getId, categoryId)
                .set(Category::getIsDiscard, true)
                .set(Category::getUpdateTime, new Date());
    }

    default MPLambdaQuery<Category> queryName(String name) {
        return lambdaQuery()
                .like(Category::getName, name)
                .eq(Category::getIsDiscard, false);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.category";
    }

    @Override
    default Class<Category> getEntityClass() {
        return Category.class;
    }
}

