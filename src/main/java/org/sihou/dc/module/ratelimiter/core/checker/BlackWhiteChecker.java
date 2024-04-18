package org.sihou.dc.module.ratelimiter.core.checker;

import org.sihou.dc.module.ratelimiter.configuration.RateLimiterProperties;
import org.sihou.dc.module.ratelimiter.core.IPChecker;
import org.sihou.dc.module.ratelimiter.model.Result;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @author zza
 * @date 2024-04-12 14:59
 */
public class BlackWhiteChecker implements IPChecker {
    private final RateLimiterProperties limiterProperties;

    public BlackWhiteChecker(RateLimiterProperties limiterProperties) {
        this.limiterProperties=limiterProperties;
    }

    /**
     * 根据请求分析IP 进行黑白名单检查
     *对于未存在于黑白名单内的IP 同白名单一致 允许请求
     * @param request
     * @return
     */
    @Override
    public Result isAllowed(HttpServletRequest request) {

        String ipAddr = getIpAddr(request);
        Set<String> whiteList = limiterProperties.getWhiteList();
        Set<String> blackList = limiterProperties.getBlackList();
        //如果白名单不为空并且存在之内 则直接通过 不会验证限流
        if (!CollectionUtils.isEmpty(whiteList)&&whiteList.contains(ipAddr)) {
            return new Result(true);
        }
        //验证黑名单 存在黑名单内则 不允许通过
        if (!CollectionUtils.isEmpty(blackList)&&blackList.contains(ipAddr)) {
            return new Result(false);
        }


          return new Result(true);
    }


    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forward-for");//负载均衡下为小写
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Forwarded-For");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 获取HttpServletRequest
     */
    public static HttpServletRequest getRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        return requestAttributes == null ? null : ((ServletRequestAttributes) requestAttributes).getRequest();
    }

}
