package com.lqc.youarezz;

import redis.clients.jedis.Jedis;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author liqicong@myhexin.com
 * @date 2020/1/14 15:30
 */
public class TestCase {
    public static void main(String[] args) {
        //定义线程池
        ThreadPoolExecutor pool = new ThreadPoolExecutor(0, 10,
                1, TimeUnit.SECONDS,
                new SynchronousQueue<>());

        //添加10个线程获取锁
        for (int i = 0; i < 10; i++) {
            pool.submit(() -> {
                try {
                    Jedis jedis = new Jedis("10.10.38.54");
                    jedis.auth("123456");
                    LockCase1 lock = new LockCase1(jedis, "lockName");
                    lock.lock();

                    //模拟业务执行15秒
                    lock.sleepBySencond(15);

                    lock.unlock();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        //当线程池中的线程数为0时，退出
        while (pool.getPoolSize() != 0) {
        }
    }
}
