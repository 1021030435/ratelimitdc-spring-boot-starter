package org.sihou.dc.module.ratelimiter.core.tokenbucket;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


/**
 * 独立实现令牌桶算法 混合令牌桶 主要特点在于初始化时 令牌桶是否为满的 代表乐观状态系统比较健康可承担最大令牌并发，反之初始化为0令牌，无法满足激烈的请求，缓慢产生令牌
 *
 * @author zza
 * @date 2024-04-17 10:21
 */
public class MixerTokenBucket {
    /**
     * 令牌桶最大容量
     */
   private int bucketCapacity;
    /**
     * 每秒产生令牌数
     */
    private  int rate;
    /**
     * 当前令牌数量
     */
    private volatile int nowCapacity;
    /**
     * 令牌刷新时间
     */
    private volatile long tokenChangedTimeMillers;

    public MixerTokenBucket(int bucketCapacity, int rate, boolean isHot) {
        if (bucketCapacity <= 0 || rate <= 0) {
            throw new IllegalArgumentException("参数不能小于0");
        }

        this.bucketCapacity = bucketCapacity;
        this.rate = rate;
        if (isHot) {
            // 此处令牌桶采取策略为初始化则认为服务可承担最大请求。
            nowCapacity = bucketCapacity;
        } else {
            // 反之初始化为0
            nowCapacity = 0;
        }
        // 刷新当前令牌更新时间
        tokenChangedTimeMillers = System.currentTimeMillis();
    }

    private volatile Object mutexDoNotUseDirectly;

    /**
     * 锁对象
     *
     * @return
     */
    private Object mutex() {
        Object mutex = mutexDoNotUseDirectly;
        if (mutex == null) {
            synchronized (this) {
                mutex = mutexDoNotUseDirectly;
                if (mutex == null) {
                    mutexDoNotUseDirectly = mutex = new Object();
                }
            }
        }
        return mutex;
    }

    /**
     * 尝试获取令牌 通过检验
     *
     * @param requestToken 请求令牌数
     * @return
     */
    public boolean tryAcquire(int requestToken) {
        if (requestToken < 0) {
            throw new IllegalArgumentException("requestToken参数不能小于0");
        }
        if (requestToken > bucketCapacity) {
            throw new IllegalArgumentException("requestToken参数不能大于令牌桶最大令牌数");
        }

        synchronized (mutex()) {
            long nowMillis = System.currentTimeMillis();
            // 发生服务器时间重置情况，
            if (nowMillis < tokenChangedTimeMillers) {
                tokenChangedTimeMillers = nowMillis;
                nowCapacity = 0;
                return false;
            }
            // 最大容量满状态
            if (nowCapacity == bucketCapacity) {
                nowCapacity = nowCapacity - requestToken;
                tokenChangedTimeMillers = nowMillis;
                return true;
            }
            // 获取秒级令牌窗口数量
            long tokenWindows = (nowMillis - tokenChangedTimeMillers) / 1000;
            Integer tokens = (int)tokenWindows * rate;
            nowCapacity=nowCapacity+tokens;
            nowCapacity=nowCapacity>bucketCapacity?bucketCapacity:nowCapacity;
            tokenChangedTimeMillers=tokenChangedTimeMillers+tokenWindows*1000;

            // 当前token满足 请求令牌数
            if (nowCapacity >= requestToken) {
                nowCapacity=nowCapacity-requestToken;
                return true;
            }

        }

        return false;
    }


    private  void test() throws InterruptedException {
        MixerTokenBucket mixerTokenBucket = new MixerTokenBucket(10,1,false);

        TimeUnit.SECONDS.sleep(1);
        new Thread(() ->{
            while (1==1){
                System.err.println(mixerTokenBucket.tryAcquire(2)+"  s1   "+ LocalDateTime.now().toString());
//           double acquire = rateLimiter.acquire(1);
//           System.err.println(acquire+"  "+ LocalDateTime.now().toString());
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() ->{
            while (1==1){
                System.err.println(mixerTokenBucket.tryAcquire(2)+" s2   "+ LocalDateTime.now().toString());
//           double acquire = rateLimiter.acquire(1);
//           System.err.println(acquire+"  "+ LocalDateTime.now().toString());
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Thread.sleep(500000);
    }

}
