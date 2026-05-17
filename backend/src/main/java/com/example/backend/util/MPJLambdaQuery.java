package com.example.backend.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.backend.dto.PageParams;
import com.example.backend.entity.IId;
import com.example.backend.mapper.IBaseMapper;
import com.github.yulichang.toolkit.JoinWrappers;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import com.github.yulichang.wrapper.segments.SelectCache;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MPJLambdaQuery<BASE extends IId, QUERY, DTO> {

    private final List<Class<?>> queryLists = new ArrayList<>();
    private final MPJLambdaWrapper<BASE> query;
    private final IBaseMapper<BASE> mapper;

    private Class<?> dtoClass = null;
    private Predicate<SelectCache> filter = cache -> true;

    public MPJLambdaQuery(IBaseMapper<BASE> mapper) {
        this.query = JoinWrappers.lambda(mapper.getEntityClass());
        this.mapper = mapper;
        queryLists.add(mapper.getEntityClass());
    }

    public <V> MPJLambdaQuery<BASE, QUERY, DTO> in(SFunction<QUERY, V> column, Collection<V> values) {
        if (values == null || values.isEmpty()) return this;

        // 去重
        Set<?> set = values instanceof Set
                ? (Set<?>) values
                : new HashSet<>(values);
        query
                .eq(set.size() == 1, column, set.iterator().next())
                .in(set.size() != 1, column, set);
        return this;
    }

    public <V> MPJLambdaQuery<BASE, QUERY, DTO> in(SFunction<QUERY, ?> column, Function<V, ?> converter, Collection<V> values) {
        if (values == null) return this;

        Set<?> set = values.stream()
                .map(converter)
                .collect(Collectors.toSet());
        query
                .eq(set.size() == 1, column, set.iterator().next())
                .in(set.size() != 1, column, set);
        return this;
    }

    public MPJLambdaQuery<BASE, QUERY, DTO> in(SFunction<QUERY, Date> column, Date time0, Date time1) {
        query
                .ge(time0 != null, column, time0)
                .le(time1 != null, column, time1);
        return this;
    }

    public MPJLambdaQuery<BASE, QUERY, DTO> in(SFunction<QUERY, BigDecimal> column, String value0, String value1) {
        if (value0 != null)
            query.ge(column, new BigDecimal(value0));
        if (value1 != null)
            query.le(column, new BigDecimal(value1));
        return this;
    }

    public <N extends Number> MPJLambdaQuery<BASE, QUERY, DTO> in(SFunction<QUERY, N> column, N value0, N value1) {
        query
                .ge(value0 != null, column, value0)
                .le(value1 != null, column, value1);
        return this;
    }

    public MPJLambdaQuery<BASE, QUERY, DTO> exist(SFunction<QUERY, ?> column, Boolean isExist) {
        query
                .isNotNull(Boolean.TRUE.equals(isExist), column)
                .isNull(Boolean.FALSE.equals(isExist), column);
        return this;
    }

    @SafeVarargs
    public final MPJLambdaQuery<BASE, QUERY, DTO> like(SFunction<QUERY, ?> column, String text, SFunction<QUERY, ?>... otherColumns) {
        if (otherColumns == null || otherColumns.length == 0) {
            query.like(text != null, column, text);
        } else {
            query.or(text != null, wrapper -> {
                wrapper.like(column, text);
                for (SFunction<QUERY, ?> otherColumn : otherColumns) {
                    wrapper.like(otherColumn, text);
                }
            });
        }
        return this;
    }

    public <V> MPJLambdaQuery<BASE, QUERY, DTO> eq(SFunction<QUERY, V> column, V value) {
        query.eq(!ObjectUtils.isEmpty(value), column, value);
        return this;
    }

    public <V, R> MPJLambdaQuery<BASE, QUERY, DTO> eq(SFunction<QUERY, R> column, Function<V, R> converter, V value) {
        query.eq(!ObjectUtils.isEmpty(value), column, converter.apply(value));
        return this;
    }

    // left join

    public <R, FOREIGN_KEY> MPJLambdaQuery<BASE, R, DTO> join(Class<DTO> dtoClass, Class<R> entityClass,
                                                              SFunction<R, FOREIGN_KEY> left, SFunction<BASE, FOREIGN_KEY> right,
                                                              SFunction<DTO, R> dtoField) {
        this.dtoClass = dtoClass;
        query.selectAssociation(entityClass, dtoField);
        query.leftJoin(entityClass, left, right);
        queryLists.add(entityClass);
        return (MPJLambdaQuery<BASE, R, DTO>) this;
    }

    public <R, FOREIGN_KEY> MPJLambdaQuery<BASE, R, DTO> joinCollection(Class<DTO> dtoClass, Class<R> entityClass,
                                                                        SFunction<R, FOREIGN_KEY> left, SFunction<BASE, FOREIGN_KEY> right,
                                                                        SFunction<DTO, Collection<R>> dtoField) {
        this.dtoClass = dtoClass;
        query.selectCollection(entityClass, dtoField);
        query.leftJoin(entityClass, left, right);
        queryLists.add(entityClass);
        return (MPJLambdaQuery<BASE, R, DTO>) this;
    }

    public MPJLambdaQuery<BASE, QUERY, DTO> filter(Predicate<SelectCache> predicate) {
        this.filter = predicate;
        return this;
    }

    public <P extends IPage<DTO>> Page<DTO> page(PageParams pageRequest) {
        Page<DTO> page = pageRequest.createPage();
        if (dtoClass == null)
            throw ServiceException.system("exception.system.dto_required");
        for (Class<?> entityClass : queryLists) {
            query.selectFilter(entityClass, filter);
        }
        queryLists.clear();
        return mapper.selectJoinPage(page, (Class<DTO>) dtoClass, query);
    }
}
