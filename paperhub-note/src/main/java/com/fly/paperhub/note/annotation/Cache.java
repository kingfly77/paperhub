package com.fly.paperhub.note.annotation;

import com.fly.paperhub.common.constants.RedisKeys;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Cache {

    String option() default "query";

    String prefix() default "";

    String connector() default RedisKeys.CONNECTOR;

    String key();

    String keyType() default "String";

    long expire() default 10;

    TimeUnit timeUnit() default TimeUnit.MINUTES;

    boolean refreshCache() default true;
}
