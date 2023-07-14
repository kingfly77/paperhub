package com.fly.paperhub.note.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("paperhub-user")
@Service
public interface UserFeignService {

    @PostMapping(value = "/user/user/getUserIdNameMapByIds", consumes = "application/json")
    Map<String, Object> getUserIdNameMapByIds(@RequestBody Map<String, Object> params);
}
