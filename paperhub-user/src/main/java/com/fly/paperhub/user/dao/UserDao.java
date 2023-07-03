package com.fly.paperhub.user.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fly.paperhub.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserDao extends BaseMapper<UserEntity> {

//    List<String> selectPasswordByUsername(@Param("username") String username);

}
