package com.fly.paperhub.note.aop;

import cn.hutool.core.bean.BeanUtil;
import com.fly.paperhub.common.constants.RedisKeys;
import com.fly.paperhub.common.utils.StringUtil;
import com.fly.paperhub.note.annotation.Cache;
import com.fly.paperhub.note.entity.NoteEntity;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
@Slf4j
public class CacheAspect {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 查询
     */
    private Object query(ProceedingJoinPoint joinPoint, String key, String keyType,
                         long expireTime, TimeUnit timeUnit, Type returnType, boolean refresh) throws Throwable {
        // 判断redis中是否存在key
        boolean hasKey = redisTemplate.hasKey(key);

        if (!hasKey) {
            // 从数据库中读取
            Object ret = joinPoint.proceed();
            // 缓存到redis中
            if ("String".equals(keyType)) {
                redisTemplate.opsForValue().set(key, ret.toString());
            } else if ("Object:Hash".equals(keyType)) {
                Map<String, Object> map = BeanUtil.beanToMap(ret);
                redisTemplate.opsForHash().putAll(key, map);
            } else if ("Object:Json".equals(keyType)) {
                redisTemplate.opsForValue().set(key, ret);
            } else {
                // TODO
                log.error("todo");
            }
            // 设置缓存时间
            redisTemplate.expire(key, expireTime, timeUnit);
            return ret;
        }

        log.debug("redis缓存命中：" + key);

        // 刷新缓存时长
        if (refresh) redisTemplate.expire(key, expireTime, timeUnit);

        // 从redis中获取
        if ("String".equals(keyType)) {
            return redisTemplate.opsForValue().get(key);
        } else if ("Object:Hash".equals(keyType)) {
            Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
            return BeanUtil.fillBeanWithMap(map, Class.forName(returnType.getTypeName()).newInstance(), false);
        } else if ("Object:Json".equals(keyType)) {
            // TODO
            log.error("todo");
            return null;
        } else {
            // TODO
            log.error("todo");
            return null;
        }
    }

    /**
     * 更新
     */
    private Object update(ProceedingJoinPoint joinPoint, String key) throws Throwable {

        // 修改数据库
        Object ret = joinPoint.proceed();

        // 删除缓存
        // TODO: rabbitMQ重试
        redisTemplate.delete(key);

        return ret;
    }


    @Pointcut("@annotation(com.fly.paperhub.note.annotation.Cache)")
    public void pointcut() { }

    @Around("pointcut()")
    public Object handleCache(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("handle cache");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
        // 方法参数名称
        String[] paramNames = u.getParameterNames(method);
        // 方法参数值
        Object[] args = joinPoint.getArgs();
        Type returnType = method.getGenericReturnType();

        Cache cache = method.getAnnotation(Cache.class);
        // 注解中的值
        String option = cache.option();
        String prefix = cache.prefix();
        String connector = cache.connector();
        String key = cache.key();
        String keyType = cache.keyType();
        long expireTime = cache.expire();
        TimeUnit timeUnit = cache.timeUnit();
        boolean refresh = cache.refreshCache();

        // 替换变量名为变量值
        int i = 0;
        StringBuilder sb = new StringBuilder();
        while (i < key.length()) {
            char c = key.charAt(i);
            if (c == '$') {
                ++i;
                if (i >= key.length() || key.charAt(i) != '{') {
                    sb.append(c);
                    continue;
                }
                int beg = ++i;
                while (i < key.length() && key.charAt(i) != '}') ++i;
                int end = i++;
                if (end >= key.length()) {
                    sb.append(key, beg, end);
                    break;
                }
                String param = key.substring(beg, end);
                int dotPos = param.indexOf('.');
                if (dotPos != -1) {
                    String objName = param.substring(0, dotPos);
                    String argName = param.substring(dotPos + 1);
                    for (int j = 0; j < paramNames.length; ++j) {
                        if (objName.equals(paramNames[j])) {
                            Field field = args[j].getClass().getDeclaredField(argName);
                            field.setAccessible(true);
                            param = String.valueOf(field.get(args[j]));
                            break;
                        }
                    }
                }
                for (int j = 0; j < paramNames.length; ++j) {
                    if (param.equals(paramNames[j])) {
                        param = String.valueOf(args[j]);
                        break;
                    }
                }
                sb.append(param);
                continue;
            }
            sb.append(c);
            ++i;
        }
        key = StringUtil.connect(connector, prefix, sb.toString());
        log.debug("key: " + key);

        if ("query".equals(option)) {
            return query(joinPoint, key, keyType, expireTime, timeUnit, returnType, refresh);
        } else if ("update".equals(option)) {
            return update(joinPoint, key);
        } else {
            // TODO
            log.error("todo");
            return null;
        }
    }

}
