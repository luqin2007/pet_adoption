package com.example.backend.mapper;

import com.example.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    User findById(Long id);

    Long findIdByUsername(String username);

    Long findIdByEmail(String email);

    User findByUsername(String username);

    User findByEmail(String email);

    void insert(User user);

    void update(User user);

    @Update("update user set password = #{password} where id = #{id}")
    void updatePassword(User user);
}
