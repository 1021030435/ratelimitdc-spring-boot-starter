package org.sihou.dc.module.ratelimiter;

import org.sihou.dc.module.ratelimiter.core.RateLimiterService;
import org.sihou.dc.module.ratelimiter.annotation.RateLimit;
import org.sihou.dc.module.ratelimiter.model.Mode;
import org.sihou.dc.module.ratelimiter.model.Result;
import org.sihou.dc.module.ratelimiter.model.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/17
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private RateLimiterService limiterService;

    @GetMapping("/limiterService/time-window")
    public String limiterServiceTimeWindow(String key) {
        Rule rule = new Rule(Mode.TIME_WINDOW); // 限流策略,设置为时间窗口
        rule.setKey(key); //限流的 key
        rule.setRate(1); //限流的速率
        rule.setRateInterval(20); //时间窗口大小，单位为秒
        Result result = limiterService.isAllowed(rule);
        if (result.isAllow()) { //如果允许访问
            return "ok";
        } else {
            //触发限流
            return "no";
        }
    }

    @GetMapping("/limiterService/token-bucket")
    public String limiterServiceTokenBucket(String key) {
        Rule rule = new Rule(Mode.TOKEN_BUCKET); // 限流策略,设置为令牌桶
        rule.setKey(key); //限流的 key
        rule.setRate(5); //每秒产生的令牌数
        rule.setBucketCapacity(10); //令牌桶容量
        rule.setRequestedTokens(1); //请求的令牌数
        Result result = limiterService.isAllowed(rule);
        if (result.isAllow()) { //如果允许访问
            return "ok";
        } else {
            //触发限流
            return "no";
        }
    }

    @GetMapping("/get")
    @RateLimit(rate = 2, rateInterval = "10s", fallbackFunction = "getFallback")
    public String get(String name) {
        return "get";
    }

    @GetMapping("/get2")
    @RateLimit(rate = 2, rateInterval = "10s", rateExpression = "${spring.ratelimiter.max:2}")
    public String get2() {
        return "get";
    }

    /**
     * 提供 wrk 压测工具压测的接口 , 测试脚本: wrk -t16 -c100 -d15s --latency http://localhost:8080/test/wrk
     */
    @GetMapping("/wrk")
    @RateLimit(rate = 100000000, rateInterval = "30s")
    public String wrk() {
        return "get";
    }

    @PostMapping("/hello/time-window-rate-limiter")
    @RateLimit(rate = 5, rateInterval = "10s", keys = {"#user.name", "#user.id"})
    public String hello(@RequestBody User user) {
        return "hello";
    }

    @PostMapping("/hello/token-bucket-rate-limiter")
    @RateLimit(mode = Mode.TOKEN_BUCKET, bucketCapacity = 10, rate = 2, requestedTokens = 2, keys = {"#user.name", "#user.id"})
    public String hello1(@RequestBody User user) {
        return "hello";
    }


    public String getFallback(String name) {
        return "命中了" + name;
    }

    public String keyFunction(String name) {
        return "keyFunction";
    }

    public static void main(String[] args) throws InterruptedException {
        test();
    }

    private static void test() throws InterruptedException {
//        MixerTokenBucket mixerTokenBucket = new MixerTokenBucket(10,1,false);

        RateLimiterService rateLimiterService = new RateLimiterService();

        Rule rule = new Rule(Mode.TOKEN_BUCKET); // 限流策略,设置为时间窗口
        rule.setKey("11"); //限流的 key
        rule.setRate(1); //限流的速率
        rule.setBucketCapacity(10);
        rule.setRequestedTokens(1);//时间窗口大小，单位为秒
        TimeUnit.SECONDS.sleep(10);
        new Thread(() ->{
            while (1==1){
                if (rateLimiterService.isAllowed(rule).isAllow()) {
                    System.err.println(rateLimiterService+"  s1   "+ LocalDateTime.now().toString());
                }
//           double acquire = rateLimiter.acquire(1);
//           System.err.println(acquire+"  "+ LocalDateTime.now().toString());
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() ->{
            while (1==1){

                if (rateLimiterService.isAllowed(rule).isAllow()) {
                    System.err.println(rateLimiterService+"  s2   "+ LocalDateTime.now().toString());
                }
//           double acquire = rateLimiter.acquire(1);
//           System.err.println(acquire+"  "+ LocalDateTime.now().toString());
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Thread.sleep(500000);
    }



}
