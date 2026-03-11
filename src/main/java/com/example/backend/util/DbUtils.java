package com.example.backend.util;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;

import java.util.List;
import java.util.function.Function;

public class DbUtils {

    public static <T> Page<T> createPage(Integer currentPage, Integer size, String sort, String order) {
        Page<T> page = Page.of(currentPage, size, true);
        if (order.equalsIgnoreCase("ASC")) {
            page.addOrder(OrderItem.asc(sort));
        } else {
            page.addOrder(OrderItem.desc(sort));
        }
        return page;
    }

    public static <T, R> Page<R> convertDto(Page<T> result, Function<T, R> converter) {
        Page<R> response = PageDTO.of(result.getCurrent(), result.getSize(), result.getTotal());
        List<T> records = result.getRecords();
        response.setRecords(records.stream()
                .map(converter)
                .toList());
        return response;
    }
}
