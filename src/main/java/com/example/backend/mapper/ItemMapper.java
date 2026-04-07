package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.backend.dto.ItemQueryParams;
import com.example.backend.entity.Item;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：
 * - (categoryId, isDiscard)
 */
@Mapper
public interface ItemMapper extends IBaseMapper<Item> {

    @SuppressWarnings("unchecked")
    default LambdaQueryWrapper<Item> queryByRequest(ItemQueryParams params) {
        LambdaQueryWrapper<Item> query = lambdaQuery();
        params.querySet(query, Item::getCategoryId, params.getCategory())
                .queryText(query, Item::getName, params.getKeyword(), Item::getDescription);
        return query;
    }

    default LambdaQueryWrapper<Item> queryByCategory(Long categoryId) {
        return lambdaQuery()
                .eq(Item::getCategoryId, categoryId)
                .ne(Item::getIsDiscard, true);
    }

    default LambdaUpdateWrapper<Item> discardItem(Long itemId) {
        return lambdaUpdate()
                .eq(Item::getId, itemId)
                .set(Item::getIsDiscard, true);
    }

    @Override
    default String getMissingMessage() {
        return "物品不存在";
    }
}
