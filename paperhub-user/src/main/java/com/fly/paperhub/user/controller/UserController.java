package com.fly.paperhub.user.controller;

import com.fly.paperhub.user.entity.UserEntity;
import com.fly.paperhub.user.service.UserService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("user/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, Object> params) throws Exception {
        // get username and password from RequestBody
        String username = (String) params.get("username");
        String password = (String) params.get("password");

        // get user by username from db
        UserEntity user = userService.getUserByName(username);

        // check password
        boolean pwdIsRight = userService.checkPassword(user, password);

        if (!pwdIsRight) {
            Map<String, Object> data = new HashMap<>();
            data.put("login_success", false);
            return data;
        }

        // create token
        String token = userService.createToken(user);

        // save token in redis
        redisTemplate.opsForValue().set(token, "", 30, TimeUnit.DAYS);

        // response
        Map<String, Object> data = new HashMap<>();
        data.put("login_success", true);
        data.put("token", token);
        data.put("uid", user.getUid());
        data.put("username", user.getUsername());
        data.put("profile", user.getProfile());

        return data;
    }

    @PostMapping("/login_with_token")
    public Map<String, Object> login_with_token(@RequestBody Map<String, Object> params) {

        // get token from RequestBody
        String token = (String) params.get("token");
        System.out.println("token: " + token);

        // check token in redis
        Object ret = redisTemplate.opsForValue().get(token);
        System.out.println("ret: " + ret);
        Map<String, Object> data = new HashMap<>();
        data.put("login_success", ret != null);
        if (ret == null) return data;

        UserEntity user = userService.decodeToken(token);
        data.put("uid", user.getUid());
        data.put("username", user.getUsername());
        data.put("profile", user.getProfile());

        return data;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, Object> params) {
        // get username and password from RequestBody
        String username = (String) params.get("username");
        String password = (String) params.get("password");
//        String profile = (String) params.get("profile");

        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setPassword(password);
//        user.setProfile(profile);

        Pair<Boolean, String> insertRet = userService.addOneUser(user);
        boolean insertSuccess = insertRet.getKey();
        String failReason = insertRet.getValue();

        Map<String, Object> data = new HashMap<>();
        data.put("register_success", insertSuccess);

        if (!insertSuccess) {
            data.put("fail_reason", failReason);
        }

        return data;
    }

    @RequestMapping("/getUserIdNameMapByIds")
    public Map<String, Object> getUserIdNameMapByIds(@RequestBody Map<String, Object> params) {
        List<Long> idList = (List<Long>) params.get("idList");
        List<UserEntity> userList = userService.listByIds(idList);
        Map<Long, String> idNameMap = new HashMap<>();
        userList.forEach(user -> {
            idNameMap.put(user.getUid(), user.getUsername());
        });
        for (Long k: idNameMap.keySet()) {
            log.debug("idNameMap: " + k + " -> " + idNameMap.get(k));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("idNameMap", idNameMap);
        return data;
    }
}
