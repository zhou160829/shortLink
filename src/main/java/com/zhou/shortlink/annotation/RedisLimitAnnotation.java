package com.zhou.shortlink.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLimitAnnotation {

    /**
     * 配置的key
     */
    String key() default "";

    /**
     * 过期时间
     */
    long expire() default 60;

    /**
     * 可以访问的次数
     */
    long permitPerSecond() default 5;

    /**
     * 拒绝访问的提示语
     * @return
     */
    String msg() default "当前访问的人数太多";
}
