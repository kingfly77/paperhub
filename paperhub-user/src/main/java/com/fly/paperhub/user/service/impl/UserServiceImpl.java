package com.fly.paperhub.user.service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fly.paperhub.user.dao.UserDao;
import com.fly.paperhub.user.entity.UserEntity;
import com.fly.paperhub.user.service.UserService;
import com.sun.istack.internal.NotNull;
import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.util.Pair;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("UserService")
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public UserEntity getUserByName(String username) {

        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);

        // username is unique
        return userDao.selectOne(wrapper);
    }

    @Override
    public boolean checkPassword(UserEntity user, String password) {
        return user != null && user.getPassword() != null && user.getPassword().equals(password);
    }

    @Override
    public String createToken(@NotNull UserEntity user) throws Exception {

        Map<String, Object> header = new HashMap<>(2);
        header.put("Type", "Jwt");
        header.put("alg", "HS256");

        Date date = new Date(System.currentTimeMillis());

        String token = JWT.create()
                .withHeader(header)
                .withExpiresAt(date)
                .withClaim("uid", user.getUid())
                .withClaim("username", user.getUsername())
                .withClaim("profile", user.getProfile())
                .sign(Algorithm.HMAC256("fly"));

        return "paperhub_login_token_" + token;
    }

    public UserEntity decodeToken(String token) {
        token = token.replace("paperhub_login_token_", "");
        DecodedJWT decodedJWT = JWT.decode(token);
        Claim uid = decodedJWT.getClaim("uid");
        Claim username = decodedJWT.getClaim("username");
        Claim profile = decodedJWT.getClaim("profile");
        UserEntity user = new UserEntity();
        user.setUid(uid.asLong());
        user.setUsername(username.asString());
        user.setProfile(profile.asString());
        return user;
    }

    @Override
    public Pair<Boolean, String> addOneUser(@NotNull UserEntity user) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("username", user.getUsername());
        if (userDao.selectCount(wrapper) != 0) {
            return new Pair<>(false, "用户名已存在");
        }
        try {
            userDao.insert(user);
        } catch (Exception e) {
            return new Pair<>(false, "未知错误");
        }
        return new Pair<>(true, "");
    }
}
