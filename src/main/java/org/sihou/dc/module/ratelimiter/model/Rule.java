package org.sihou.dc.module.ratelimiter.model;

import org.springframework.util.ObjectUtils;

/**
 * Created by kl on 2017/12/29. Content : 限流器规则信息
 */
public class Rule {

    private String key;
    // 限流的速率或每秒产生的令牌数
    private int rate;
    // 时间窗口大小，单位为秒
    private long rateInterval;
    private Mode mode;
    // 令牌桶容量
    private int bucketCapacity;
    // 请求的令牌数
    private int requestedTokens;
    private String fallbackFunction;

    public Rule(String key, int rate, int rateInterval, Mode mode, int bucketCapacity, int requestedTokens) {
        this.key = key;
        this.rate = rate;
        this.rateInterval = rateInterval;
        this.mode = mode;
        this.bucketCapacity = bucketCapacity;
        this.requestedTokens = requestedTokens;
    }

    public Rule(String key, int rate, Mode mode) {
        this.key = key;
        this.rate = rate;
        this.mode = mode;
    }

    public Rule(Mode mode) {
        this.mode = mode;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public long getRateInterval() {
        return rateInterval;
    }

    public void setRateInterval(int rateInterval) {
        this.rateInterval = rateInterval;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public int getBucketCapacity() {
        return bucketCapacity;
    }

    public void setBucketCapacity(int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
    }

    public int getRequestedTokens() {
        return requestedTokens;
    }

    public void setRequestedTokens(int requestedTokens) {
        this.requestedTokens = requestedTokens;
    }

    public String getFallbackFunction() {
        return fallbackFunction;
    }

    public void setFallbackFunction(String fallbackFunction) {
        this.fallbackFunction = fallbackFunction;
    }

    /**
     * 判断是否热更新属性
     * @param rate
     * @param bucketCapacity
     * @param rateInterval
     * @return
     */
    public Boolean checkRuleHotChanged(int rate, int bucketCapacity, long rateInterval) {
        return !ObjectUtils.nullSafeEquals(rateInterval, this.rateInterval)
            || !ObjectUtils.nullSafeEquals(bucketCapacity, this.bucketCapacity)
            || !ObjectUtils.nullSafeEquals(rate, this.rate);
    }

}
