package org.sihou.dc.module.ratelimiter.configuration;

import org.sihou.dc.module.ratelimiter.core.RateLimitAspectHandler;
import org.sihou.dc.module.ratelimiter.core.RateLimiterService;
import org.sihou.dc.module.ratelimiter.core.RuleProvider;
import org.sihou.dc.module.ratelimiter.core.checker.BlackWhiteChecker;
import org.sihou.dc.module.ratelimiter.web.RateLimitExceptionHandler;
import org.sihou.dc.module.ratelimiter.core.IPChecker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author kl (http://kailing.pub)
 * @since 2021/3/16
 */
@Configuration
@ConditionalOnProperty(prefix = RateLimiterProperties.PREFIX, name = "enabled", havingValue = "true")
//@AutoConfigureAfter(RedisAutoConfiguration.class)
//@EnableConfigurationProperties(RateLimiterProperties.class)
@Import({RateLimitAspectHandler.class, RateLimitExceptionHandler.class,RateLimiterProperties.class})
public class RateLimiterAutoConfiguration {

    private final RateLimiterProperties limiterProperties;
//    public final static String REDISSON_BEAN_NAME = "rateLimiterRedissonBeanName";

    public RateLimiterAutoConfiguration(RateLimiterProperties limiterProperties) {
        this.limiterProperties = limiterProperties;
    }

//    @Bean(name = REDISSON_BEAN_NAME, destroyMethod = "shutdown")
//    RedissonClient redisson() {
//        Config config = new Config();
//        if (limiterProperties.getRedisClusterServer() != null) {
//            config.useClusterServers().setPassword(limiterProperties.getRedisPassword())
//                    .addNodeAddress(limiterProperties.getRedisClusterServer().getNodeAddresses());
//        } else {
//            config.useSingleServer().setAddress(limiterProperties.getRedisAddress())
//                    .setDatabase(limiterProperties.getRedisDatabase())
//                    .setPassword(limiterProperties.getRedisPassword());
//        }
//        config.setEventLoopGroup(new NioEventLoopGroup());
//        return Redisson.create(config);
//    }

    @Bean
    @ConditionalOnMissingBean
    public RuleProvider bizKeyProvider() {
        return new RuleProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimiterService rateLimiterInfoProvider() {
        return new RateLimiterService();
    }

    @Bean
    @ConditionalOnMissingBean
    public IPChecker ipChecker() {
        return new BlackWhiteChecker(limiterProperties);
    }

}
