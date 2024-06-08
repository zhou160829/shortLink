package com.zhou.shortlink.component;

import java.util.concurrent.TimeUnit;

public interface RLock {

    public void lock();


    public boolean tryLock();


    public boolean tryLock(long time, TimeUnit timeUnit) throws InterruptedException;

    void unlock();
}
