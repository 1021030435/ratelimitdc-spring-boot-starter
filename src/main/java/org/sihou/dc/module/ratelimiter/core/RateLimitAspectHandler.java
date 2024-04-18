package org.sihou.dc.module.ratelimiter.core;

import org.sihou.dc.module.ratelimiter.annotation.RateLimit;
import org.sihou.dc.module.ratelimiter.configuration.RateLimiterProperties;
import org.sihou.dc.module.ratelimiter.exception.RateLimitException;
import org.sihou.dc.module.ratelimiter.model.Result;
import org.sihou.dc.module.ratelimiter.model.Rule;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by kl on 2017/12/29. Content : 切面拦截处理器
 */
@Aspect
@Component
@Order(0)
public class RateLimitAspectHandler {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitAspectHandler.class);

    private final RateLimiterService rateLimiterService;
    private final RuleProvider ruleProvider;
    private final RateLimiterProperties rateLimiterProperties;
    private final IPChecker ipChecker;

    public RateLimitAspectHandler(RateLimiterService lockInfoProvider, RuleProvider ruleProvider,
        RateLimiterProperties rateLimiterProperties,IPChecker ipChecker) {
        this.rateLimiterService = lockInfoProvider;
        this.ruleProvider = ruleProvider;
        this.rateLimiterProperties = rateLimiterProperties;
        this.ipChecker = ipChecker;

    }

    @Around(value = "@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {

       //如果ip禁用 则抛出异常
        if ( !ipChecker.isAllowed(getRequest()).isAllow()) {
            throw new RateLimitException("the way not allow", null);
        }



        Rule rule = ruleProvider.getRateLimiterRule(joinPoint, rateLimit);

        Result result = rateLimiterService.isAllowed(rule);
        boolean allowed = result.isAllow();
        if (!allowed) {
            logger.info("Trigger current limiting,key:{}", rule.getKey());
            if (StringUtils.hasLength(rule.getFallbackFunction())) {
                return ruleProvider.executeFunction(rule.getFallbackFunction(), joinPoint);
            }
//            long extra = result.getExtra();
            throw new RateLimitException("Too Many Requests", rule.getMode());
        }
        return joinPoint.proceed();
    }

    /**
     * 获取HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return requestAttributes == null ? null : ((ServletRequestAttributes) requestAttributes).getRequest();
    }

}
