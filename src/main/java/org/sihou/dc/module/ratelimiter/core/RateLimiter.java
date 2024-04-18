package org.sihou.dc.module.ratelimiter.core;

import org.sihou.dc.module.ratelimiter.model.Result;
import org.sihou.dc.module.ratelimiter.model.Rule;

/**
 * @author kl (http://kailing.pub)
 * @since 2022/8/23
 */
public interface RateLimiter {

    Result isAllowed(Rule rule);
}
