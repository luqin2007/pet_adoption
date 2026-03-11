package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.example.backend.entity.IId;
import com.example.backend.util.ServiceException;

public interface IBaseMapper<T extends IId> extends BaseMapper<T> {

    default LambdaQueryWrapper<T> lambdaQuery() {
        return Wrappers.lambdaQuery();
    }

    default LambdaUpdateWrapper<T> lambdaUpdate() {
        return Wrappers.lambdaUpdate();
    }

    default <V> void deleteBy(SFunction<T, V> column, V value) {
        delete(lambdaQuery().eq(column, value));
    }

    default <V> void updateById(Long id, SFunction<T, V> column, V value) {
        update(lambdaUpdate().set(column, value).eq(T::getId, id));
    }

    default T requireById(Long id, String message) {
        return requireExist(selectById(id), message);
    }

    default <V> T requireOne(SFunction<T, V> field, V value, String message) {
        Wrapper<T> wrapper = Wrappers.<T>lambdaQuery().eq(field, value);
        return requireExist(selectOne(wrapper), message);
    }

    default T requireOne(Wrapper<T> queryWrapper, String message) {
        return requireExist(selectOne(queryWrapper), message);
    }

    private T requireExist(T obj, String message) {
        if (obj == null) throw ServiceException.notFound(message);
        return obj;
    }
}
