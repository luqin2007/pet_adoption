package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.backend.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {

//    @Select("select * from user where id = #{id}")
//    User findById(Long id);
//
//    @Select("select id from user where username = #{username}")
//    Long findIdByUsername(String username);
//
//    @Select("select id from user where email = #{email}")
//    Long findIdByEmail(String email);
//
//    @Select("select * from user where username = #{username}")
//    User findByUsername(String username);
//
//    @Select("select * from user where email = #{email}")
//    User findByEmail(String email);
//
//    @Select("select * from user")
//    IPage<User> findAll(IPage<?> page);
//
//    @Update("insert into user (username, password, email, role, avatar, createTime, updateTime) " +
//                      "values (#{username}, #{password}, #{email}, #{role}, #{avatar}, #{createTime}, #{updateTime})")
//    void insert(User user);
//
//    @Update("update user set username = #{username}, password = #{password}, email = #{email}, role = #{role}, avatar = #{avatar}, updateTime = #{updateTime} where id = #{id}")
//    int update(User user);
//
//    @Update("update user set password = #{password} where email = #{email}")
//    int updatePassword(String email, String password);
//
//    @Delete("delete from user where id = #{id}")
//    void deleteById(Long id);

//    User selectByUsername(String username);
//
//    User selectByEmail(String email);
//
//    boolean existsByUsername(String username);
//
//    boolean existsByEmail(String email);
//
//    int updatePasswordByEmail(String email, String password);
}
