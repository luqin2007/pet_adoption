package com.example.backend.util;

import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class Bits {

    public static <T> Integer zip(Map<T, Integer> map, Collection<T> values) {
        if (ObjectUtils.isEmpty(values)) return 0;
        return values.stream().map(map::get).reduce(0, Bits::or);
    }

    public static <T> Stream<T> unzip(Map<T, Integer> map, Integer bit) {
        if (bit == null) return Stream.of();
        return map.entrySet().stream()
                .filter(entry -> (entry.getValue() & bit) == entry.getValue())
                .map(Map.Entry::getKey)
                .distinct();
    }

    public static <T> Stream<T> unzip2(Map<Integer, List<T>> map, Integer bit) {
        if (bit == null) return Stream.of();
        return map.entrySet().stream()
                .filter(entry -> (entry.getKey() & bit) == entry.getKey())
                .map(Map.Entry::getValue)
                .flatMap(List::stream)
                .distinct();
    }

    public static Integer rezip(Map<Integer, Integer> map, Integer bit) {
        if (bit == null) return 0;
        return map.entrySet().stream()
                .filter(entry -> (entry.getKey() & bit) == entry.getKey())
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .reduce(0, Bits::or);
    }

    public static Integer or(Integer a, Integer b) {
        if (a == null) return b;
        if (b == null) return a;
        return a | b;
    }

    public static boolean match(Integer bit, int checkBit) {
        return bit != null && (bit & checkBit) == checkBit;
    }

    public static boolean matchOr(Integer bit, int... checkBits) {
        if (bit == null) return false;
        for (int checkBit : checkBits) {
            if ((bit & checkBit) == checkBit)
                return true;
        }
        return false;
    }
}
