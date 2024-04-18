package org.sihou.dc.module.ratelimiter.core.tokenbucket;

import java.util.concurrent.ConcurrentHashMap;

import org.sihou.dc.module.ratelimiter.core.RateLimiter;
import org.sihou.dc.module.ratelimiter.model.Result;
import org.sihou.dc.module.ratelimiter.model.Rule;
import org.springframework.stereotype.Component;

/**
 * @author zza
 * @since 2024年4月16日
 */
@Component
public class TokenBucketMemoryRateLimiter implements RateLimiter {

    // 储存拦截信息 关于内存回收应该使用异步线程遍历删除过期时间很长的信息
    private ConcurrentHashMap<String, MixerTokenBucket> mixerTokenBucketMap = new ConcurrentHashMap<>();

    public TokenBucketMemoryRateLimiter() {
    }

    @Override
    public Result isAllowed(Rule rule) {
        int bucketCapacity = rule.getBucketCapacity();
        int rate = rule.getRate();
        int requestedTokens = rule.getRequestedTokens();
        String key = rule.getKey();
        MixerTokenBucket mixBucket = mixerTokenBucketMap.computeIfAbsent(key, (k) -> {
            return  new MixerTokenBucket(bucketCapacity, rate, true);
        });

        return new Result(mixBucket.tryAcquire(requestedTokens));
    }


}

