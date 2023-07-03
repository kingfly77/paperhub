package com.fly.paperhub.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fly.paperhub.user.entity.UserEntity;
import com.sun.istack.internal.NotNull;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.util.Pair;

import java.util.Map;

public interface UserService extends IService<UserEntity> {

    UserEntity getUserByName(String username);

    boolean checkPassword(UserEntity user, String password);

    String createToken(@NotNull UserEntity user) throws Exception;

    UserEntity decodeToken(String token);

    Pair<Boolean, String> addOneUser(@NotNull UserEntity user);

}
