package com.zhou.shortlink.aspect;

import cn.hutool.core.util.StrUtil;
import com.zhou.shortlink.annotation.RedisLimitAnnotation;
import com.zhou.shortlink.exceptions.RedisLimitException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@Component
@Aspect
@Slf4j
public class RedisLimiterAspect {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private DefaultRedisScript<Boolean> ipLimitLua;


    @Pointcut("@annotation(com.zhou.shortlink.annotation.RedisLimitAnnotation)")
    public void myLimiterPointCut() {
    }


    @Before("myLimiterPointCut()")
    public void limiter(JoinPoint joinPoint) {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();

        RedisLimitAnnotation annotation = method.getAnnotation(RedisLimitAnnotation.class);

        if (annotation != null) {
            String key = annotation.key();
            String className = method.getDeclaringClass().getName();
            String methodName = method.getName();

            log.info("key是{},className是{},methodName是{}", key, className, methodName);

            if (StrUtil.isBlank(key)) {
                throw new RedisLimitException("限流key不能为空");
            }

            long permitPerSecond = annotation.permitPerSecond();
            long expire = annotation.expire();

            Boolean execute = stringRedisTemplate.execute(ipLimitLua, List.of(key), String.valueOf(permitPerSecond), String.valueOf(expire));

            if (Boolean.FALSE.equals(execute)) {
                throw new RedisLimitException(annotation.msg());
            }
        }
    }

}
