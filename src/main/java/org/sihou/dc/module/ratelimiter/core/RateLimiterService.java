package org.sihou.dc.module.ratelimiter.core;

import org.sihou.dc.module.ratelimiter.core.timewindow.TimeWindowMemoryRateLimiter;
import org.sihou.dc.module.ratelimiter.core.tokenbucket.TokenBucketMemoryRateLimiter;
import org.sihou.dc.module.ratelimiter.model.Mode;
import org.sihou.dc.module.ratelimiter.model.Result;
import org.sihou.dc.module.ratelimiter.model.Rule;

import java.util.HashMap;
import java.util.Map;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/16
 */
public class RateLimiterService {

    private static final Map<Mode, RateLimiter> RATE_LIMITER_FACTORY = new HashMap<>();

    public RateLimiterService() {
        RATE_LIMITER_FACTORY.put(Mode.TIME_WINDOW, new TimeWindowMemoryRateLimiter());
        RATE_LIMITER_FACTORY.put(Mode.TOKEN_BUCKET, new TokenBucketMemoryRateLimiter());
    }

    public Result isAllowed(Rule rule) {
        RateLimiter rateLimiter = RATE_LIMITER_FACTORY.get(rule.getMode());
        return rateLimiter.isAllowed(rule);
    }



}
