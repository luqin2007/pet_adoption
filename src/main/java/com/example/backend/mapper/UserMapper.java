package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.example.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * 索引：
 * - (username)
 * - (email)
 */
@Mapper
public interface UserMapper extends IBaseMapper<User> {

    /**
     * 根据用户名查询用户数据
     * - 索引：(username)
     * - 查询：[User]
     *
     * @param username 用户名
     */
    default LambdaQueryWrapper<User> queryByUser(String username) {
        return lambdaQuery().eq(User::getUsername, username);
    }

    /**
     * 根据邮箱查询用户数据
     * - 索引：(email)
     * - 查询：[User]
     *
     * @param email 邮箱
     */
    default LambdaQueryWrapper<User> queryByEmail(String email) {
        return lambdaQuery().eq(User::getEmail, email);
    }

    /**
     * 获取用于显示的用户数据
     * - 查询：[User(id, username, avatar)]
     *
     * @param userId 用户 id
     */
    default LambdaQueryWrapper<User> queryUsernameAndAvatar(Long userId) {
        return lambdaQuery()
                .eq(User::getId, userId)
                .select(User::getId, User::getUsername, User::getAvatar);
    }

    /**
     * 批量获取用于显示的用户数据
     * - 查询：[User(id, username, avatar)]
     *
     * @param userIds 用户 id
     */
    default LambdaQueryWrapper<User> queryUsernameAndAvatar(Collection<Long> userIds) {
        return lambdaQuery()
                .in(User::getId, userIds)
                .select(User::getId, User::getUsername, User::getAvatar);
    }

    /**
     * 批量获取用户邮件地址
     * - 查询：[User(id, email)]
     *
     * @param userIds 用户 id
     */
    default LambdaQueryWrapper<User> queryIdAndEmails(Collection<Long> userIds) {
        return lambdaQuery()
                .in(User::getId, userIds)
                .select(User::getId, User::getEmail);
    }

    /**
     * 根据角色查询用户数据，需要位运算
     * - 查询：[User]
     *
     * @param role   角色
     * @param column 查询列
     * @param <R>    列类型
     */
    default <R> List<R> selectObjsByRole(Integer role, SFunction<User, R> column) {
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery()
                .select(column)
                .apply("role & {0} = {0}", role);
        return selectObjs(wrapper);
    }
}
