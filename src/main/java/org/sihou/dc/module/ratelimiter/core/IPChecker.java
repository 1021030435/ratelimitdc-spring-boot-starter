package org.sihou.dc.module.ratelimiter.core;

import org.sihou.dc.module.ratelimiter.model.Result;

import javax.servlet.http.HttpServletRequest;

/**
 * 检查request请求IP黑白名单
 * @author za
 * @since 2024年4月12日14:49:35
 */
public interface IPChecker {
    /**
     * 根据请求分析IP 进行黑白名单检查
     * @param request
     * @return
     */
    Result isAllowed(HttpServletRequest request);
}
