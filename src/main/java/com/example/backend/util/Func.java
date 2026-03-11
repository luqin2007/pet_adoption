package com.example.backend.util;

import com.example.backend.entity.IId;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Func {

    public static <T extends IId> Collector<T, ?, Map<Long, T>> toIdMap() {
        return Collectors.toMap(T::getId, Function.identity());
    }

    public static <T extends IId, R> Collector<T, ?, Map<Long, R>> toIdMap(Function<T, R> converter) {
        return Collectors.toMap(T::getId, converter);
    }
}
