package com.example.backend.mapper;

import com.example.backend.dto.ItemQueryParams;
import com.example.backend.entity.Item;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (categoryId, isDiscard)
 */
@Mapper
public interface ItemMapper extends IBaseMapper<Item> {

    default MPLambdaQuery<Item> queryByRequest(ItemQueryParams params) {
        return lambdaQuery()
                .in(Item::getCategoryId, params.getCategory())
                .like(Item::getName, params.getKeyword(), Item::getDescription);
    }

    default MPLambdaQuery<Item> queryByCategory(Long categoryId) {
        return lambdaQuery()
                .eq(Item::getCategoryId, categoryId)
                .ne(Item::getIsDiscard, true);
    }

    default MPLambdaUpdate<Item> discardItem(Long itemId) {
        return lambdaUpdate()
                .eq(Item::getId, itemId)
                .set(Item::getIsDiscard, true);
    }

    @Override
    default String getMissingMessage() {
        return "exception.not_found.item";
    }

    @Override
    default Class<Item> getEntityClass() {
        return Item.class;
    }
}

