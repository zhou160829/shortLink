package com.zhou.shortlink.component;

import cn.hutool.core.util.IdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class DistributedLockFactory {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private String lockName;
    private String uuidValue;

    public DistributedLockFactory() {
        this.uuidValue = IdUtil.simpleUUID();//UUID
    }

    public RLock getRedisLock(String key) {
        lockName = key;
        return new RedisLock(stringRedisTemplate, lockName, uuidValue);
    }
}

