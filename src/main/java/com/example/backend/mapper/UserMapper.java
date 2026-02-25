package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("select * from users where id = #{id}")
    User findById(Long id);

    @Select("select id from users where username = #{username}")
    Long findIdByUsername(String username);

    @Select("select id from users where email = #{email}")
    Long findIdByEmail(String email);

    @Select("select * from users where username = #{username}")
    User findByUsername(String username);

    @Select("select * from users where email = #{email}")
    User findByEmail(String email);

    @Select("select * from users")
    IPage<User> findAll(IPage<?> page);

    @Update("insert into users (username, password, email, role, avatar, createTime, updateTime) " +
                      "values (#{username}, #{password}, #{email}, #{role}, #{avatar}, #{createTime}, #{updateTime})")
    void insert(User user);

    @Update("update users set username = #{username}, password = #{password}, email = #{email}, role = #{role}, avatar = #{avatar}, updateTime = #{updateTime} where id = #{id}")
    void update(User user);

    @Update("update users set password = #{password} where id = #{id}")
    void updatePassword(User user);

    @Delete("delete from users where id = #{id}")
    void delete(Long id);
}
