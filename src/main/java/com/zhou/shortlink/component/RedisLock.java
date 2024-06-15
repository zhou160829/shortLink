package com.zhou.shortlink.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisLock implements RLock {

    StringRedisTemplate stringRedisTemplate;

    private String lockName;

    private String uuidValue;

    private long expireTime;

    public RedisLock(StringRedisTemplate stringRedisTemplate, String lockName, String uuidValue) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.lockName = lockName;
        this.uuidValue = uuidValue + ":" + Thread.currentThread().threadId();
        this.expireTime = 30L;
    }


    @Override
    public void lock() {
        tryLock();
    }

    @Override
    public boolean tryLock() {
        try {
            tryLock(-1L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("上锁失败，异常为{}", e.getMessage());
        }
        return false;
    }

    /**
     * 干活的，实现加锁功能，实现这一个干活的就OK，全盘通用
     *
     * @param time
     * @param unit
     * @return
     * @throws InterruptedException
     */
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        if (time != -1L) {
            this.expireTime = unit.toSeconds(time);
        }

        int maxAttempts = 3;

        String script =
                    "if redis.call('exists',KEYS[1]) == 0 or redis.call('hexists',KEYS[1],ARGV[1]) == 1 then " +
                            "redis.call('hincrby',KEYS[1],ARGV[1],1) " +
                            "redis.call('expire',KEYS[1],ARGV[2]) " +
                            "return 1 " +
                            "else " +
                            "return 0 " +
                            "end";

        while (maxAttempts > 0) {
            if (Boolean.TRUE.equals(stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuidValue, String.valueOf(expireTime)))) {
                this.renewExpire();
                return true;
            }
            TimeUnit.MILLISECONDS.sleep(50);
            maxAttempts--;
        }
        return false;
    }

    @Override
    public void unlock() {
        String script =
                "if redis.call('HEXISTS',KEYS[1],ARGV[1]) == 0 then " +
                        "   return nil " +
                        "elseif redis.call('HINCRBY',KEYS[1],ARGV[1],-1) == 0 then " +
                        "   return redis.call('del',KEYS[1]) " +
                        "else " +
                        "   return 0 " +
                        "end";
        System.out.println("lockName: " + lockName);
        System.out.println("uuidValue: " + uuidValue);
        System.out.println("expireTime: " + expireTime);
        Long flag = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuidValue, String.valueOf(expireTime));
        if (flag == null) {
            throw new RuntimeException("This lock doesn't EXIST");
        }
    }

    private void renewExpire() {
        String script =
                "if redis.call('HEXISTS',KEYS[1],ARGV[1]) == 1 then " +
                        "return redis.call('expire',KEYS[1],ARGV[2]) " +
                        "else " +
                        "return 0 " +
                        "end";
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (Boolean.TRUE.equals(stringRedisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuidValue, String.valueOf(expireTime)))) {
                    renewExpire();
                }
            }
        }, (this.expireTime * 1000) / 3);
    }

}
