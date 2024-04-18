package org.sihou.dc.module.ratelimiter.configuration;

import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/16
 */
@ConfigurationProperties(prefix = RateLimiterProperties.PREFIX)
@Data
public class RateLimiterProperties {

    public static final String PREFIX = "spring.ratelimiter";


    private int statusCode = 429;


    private Set<String> whiteList ;

    private Set<String> blackList ;

    private String responseBody = "{\"code\":429,\"msg\":\"Too Many Requests\"}";

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }



}
