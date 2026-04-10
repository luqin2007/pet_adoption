package com.example.backend.mapper;

import com.example.backend.entity.User;
import com.example.backend.util.MPLambdaQuery;
import com.example.backend.util.MPLambdaUpdate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 索引：<br>
 * - (username)<br>
 * - (email)
 */
@Mapper
public interface UserMapper extends IBaseMapper<User> {

    /**
     * 根据用户名查询用户数据<br>
     * - 索引：(username)
     */
    default MPLambdaQuery<User> queryByUser(String username) {
        return lambdaQuery().eq(User::getUsername, username);
    }

    /**
     * 根据邮箱查询用户数据<br>
     * - 索引：(email)
     */
    default MPLambdaQuery<User> queryByEmail(String email) {
        return lambdaQuery().eq(User::getEmail, email);
    }

    /**
     * 更新用户头像
     */
    default MPLambdaUpdate<User> updateAvatar(Long userId, String filename) {
        return lambdaUpdate()
                .eq(User::getId, userId)
                .set(User::getAvatar, filename);
    }

    /**
     * 根据角色查询用户数据，需要位运算
     */
    default MPLambdaQuery<User> queryByRole(Integer role) {
        return lambdaQuery().apply("role & {0} = {0}", role);
    }

    @Override
    default String getMissingMessage() {
        return "用户不存在";
    }

    @Override
    default Class<User> getEntityClass() {
        return User.class;
    }
}
