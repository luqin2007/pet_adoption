package com.example.backend.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;

import java.util.List;
import java.util.function.Function;

public class DbUtils {

    public static <T, R> Page<R> convertDto(Page<T> result, Function<T, R> converter) {
        Page<R> response = PageDTO.of(result.getCurrent(), result.getSize(), result.getTotal());
        List<T> records = result.getRecords();
        response.setRecords(records.stream()
                .map(converter)
                .toList());
        return response;
    }
}
