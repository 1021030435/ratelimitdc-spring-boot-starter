package org.sihou.dc.module.ratelimiter.core.timewindow;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.sihou.dc.module.ratelimiter.core.RateLimiter;
import org.sihou.dc.module.ratelimiter.model.Result;
import org.sihou.dc.module.ratelimiter.model.Rule;

/**
 * @author zza
 * @since 2024年4月7日15:56:56
 */
public class TimeWindowMemoryRateLimiter implements RateLimiter {

    // 储存拦截信息 关于内存回收应该使用异步线程遍历删除过期时间很长的信息
    private ConcurrentHashMap<String, TimeLimitHandleHolder> timeLimitHandlerMap = new ConcurrentHashMap<>();

    public TimeWindowMemoryRateLimiter() {}

    @Override
    public Result isAllowed(Rule rule) {
        String key = rule.getKey();
        TimeLimitHandleHolder timeLimitHandleHolder = timeLimitHandlerMap.computeIfAbsent(key, (k) -> {
            TimeLimitHandleHolder holder = new TimeLimitHandleHolder();
            holder.setRule(rule);
            holder.setRequestCount(new AtomicLong());
            return holder;
        });

        Rule holdRule = timeLimitHandleHolder.getRule();
        // 检查热更新 参数
        if (holdRule != rule
            && holdRule.checkRuleHotChanged(rule.getRate(), rule.getBucketCapacity(), rule.getRateInterval())) {
            // doublecheck 更新原规则参数
            synchronized (timeLimitHandlerMap) {
                if (holdRule != rule
                    && holdRule.checkRuleHotChanged(rule.getRate(), rule.getBucketCapacity(), rule.getRateInterval())) {
                    timeLimitHandleHolder.setRule(rule);
                }
            }
        }

        boolean isAllow = limitCheck(key);

        return new Result(isAllow);
    }

    // static List<Object> getKeys(String key) {
    // String prefix = "request_rate_limiter.{".concat(key);
    // String keys = prefix.concat("}");
    //
    // return Collections.singletonList(keys);
    // }
    //
    // public static void main(String[] args) {
    // long l = new AtomicLong().incrementAndGet();
    // System.err.println(l);
    // }

    /**
     * 检查是否限流 关于时间窗口算法说明
     * 1-.先incr请求数 .
     * 2-.查看是否超出限流频次，在并发情况下，时间窗口的算法会造成超流情况，这取决于时间窗口的选择起始问题。
     * 3-.未超过限流频次则正常请求，超出速率，进入锁区间进行判断
     * 4-.锁区间内先进行时间窗口判断，对于超过区间的进行时间窗口以及请求数更新，返回限流判断
     * 5-.对于锁外排队线程，在上一线程让出锁区间后进入锁区间，此时有可能发生时间窗口与请求数变化，此时会重新incr请求数以返回是否限流 大佬们如果有更优算法可以修改此算法。
     *
     * @param key 限流key 由于sona对于参数加锁的报警，怀疑参数修改导致锁失效，其实是误报，此处选择手动获取timeLimitHandleHolder，已解决sona误报问题。
     * @return
     */
    private boolean limitCheck(String key) {
        TimeLimitHandleHolder timeLimitHandleHolder = timeLimitHandlerMap.get(key);
        AtomicLong requestCount = timeLimitHandleHolder.getRequestCount();
        Rule rule = timeLimitHandleHolder.getRule();

        Long rateInterval = rule.getRateInterval();
        Integer rate = rule.getRate();
        Long windowStartMillis = timeLimitHandleHolder.getTimeWindowStartMillis();
        if (windowStartMillis == null) {
            windowStartMillis = System.currentTimeMillis();
            timeLimitHandleHolder.setTimeWindowStartMillis(windowStartMillis);
        }

        // 1.首先自增请求数量
        // 2.判断是否超过rate
        if (requestCount.incrementAndGet() > rate) {
            // timeLimitHandleHolder对象唯一，此对象与key相关联。以实现对某一key的单锁。
            synchronized (timeLimitHandleHolder) {
                // 3.进行验证 当前时间与时间窗口是否超出
                long nowMillis = System.currentTimeMillis();
                // 3.1重新获取时间窗口开始时间 多线程可能有其他线程更新此字段
                windowStartMillis = timeLimitHandleHolder.getTimeWindowStartMillis();
                long requestInterval = nowMillis - windowStartMillis;
                // 上次时间窗口至本次请求的时间差大于时间窗口
                if (requestInterval > rateInterval * 1000) {
                    // 重置 时间窗口
                    timeLimitHandleHolder.setTimeWindowStartMillis(nowMillis);
                    // 重置请求数 手动设置为1 请求数
                    requestCount.set(1);
                    // 返回允许
                    return true;
                } else {
                    // 如果小于rate数则说明另一线程已经将时间窗口过期 此时窗口请求数已经被重置 需要新增请求数
                    if (requestCount.get() <= rate) {
                        requestCount.incrementAndGet();
                    }
                    // 返回结果
                    return requestCount.get() <= rate;
                }

            }

        }
        // 未超出频次则正常请求
        return true;

    }

}
