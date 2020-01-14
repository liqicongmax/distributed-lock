package com.lqc.youarezz;

import com.lqc.youarezz.lock.RedisLock;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.time.LocalTime;

import static com.lqc.youarezz.constant.LockConstants.OK;

/**
 * @author liqicong@myhexin.com
 * @date 2020/1/14 14:12
 */
public class LockCase1 extends RedisLock {
    public LockCase1(Jedis jedis, String name) {
        super(jedis, name);
    }

    @Override
    public void lock() {
        while(true){
            String result = jedis.set(lockKey, lockValue,new SetParams().nx().px(300));
            if(OK.equals(result)){
                System.out.println(Thread.currentThread().getId()+"加锁成功!");
                // 设置是否开启过期时间更新
                isOpenExpirationRenewal=true;
                //
                scheduleExpirationRenewal();
                break;
            }
            System.out.println("线程id:"+Thread.currentThread().getId() + "获取锁失败，休眠10秒!时间:"+ LocalTime.now());
            //休眠10秒
            sleepBySencond(10);
        }
    }

    /**
     * 线程执行阻塞时间超过设定的过期时间，这个key就木了，此时另外一个线程也可以获取key，但是这就造成了2个线程
     * 同时执行的问题。
     *  解决了错误释放锁的问题。
     *  在线程1超时完成任务结束后，准备去获取锁然后释放，这时候线程2超时了，线程3占了lockkey设置了新值
     *
     *  如何解决非原子性的问题，当线程1释放锁的时候，正好线程1这个锁超时了，这时候线程2进来是可以正确拿到锁的，然后线程1的释放锁把线程2的锁给释放了，
     *  所以应该使用原子方法才行
     */
    @Override
    public void unlock() {
        String checkAndDelScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                "return redis.call('del', KEYS[1]) " +
                "else " +
                "return 0 " +
                "end";
        jedis.eval(checkAndDelScript, 1, lockKey, lockValue);
        isOpenExpirationRenewal=false;
    }
}
