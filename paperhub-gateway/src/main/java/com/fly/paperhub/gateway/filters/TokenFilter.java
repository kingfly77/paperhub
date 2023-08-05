package com.fly.paperhub.gateway.filters;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.fly.paperhub.common.constants.Headers;
import com.fly.paperhub.common.constants.RedisKeys;
import com.fly.paperhub.common.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TokenFilter extends AbstractGatewayFilterFactory {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 未登录；或登录已过期，需要重新登录
     */
    private static Mono<Void> responseLogin(ServerWebExchange exchange, String msg) {
        log.debug("unauthorized token: " + msg);
        ServerHttpResponse response = exchange.getResponse();
        //设置响应头
        response.getHeaders().add("ContentType","application/json;charset=utf-8");
        //设置状态码
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        // 封装响应数据
        DataBuffer dataBuffer = response.bufferFactory().wrap(msg.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(dataBuffer));
    }

    @Override
    public GatewayFilter apply(Object config) {
        return ((exchange, chain) -> {
            HttpHeaders headers = exchange.getRequest().getHeaders();
            String token = headers.getFirst(Headers.TOKEN);
            if (StrUtil.isBlank(token)) {
               return responseLogin(exchange, "尚未登录");
            }
            // check token in redis
            Object ret = redisTemplate.opsForValue().get(token);
            System.out.println("ret: " + ret);
            if (ret == null) {
                return responseLogin(exchange, "登录已过期");
            }
            return chain.filter(exchange);
        });
    }
}
