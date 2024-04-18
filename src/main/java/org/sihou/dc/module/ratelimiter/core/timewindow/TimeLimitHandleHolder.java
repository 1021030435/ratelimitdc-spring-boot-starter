package org.sihou.dc.module.ratelimiter.core.timewindow;

import java.util.concurrent.atomic.AtomicLong;

import org.sihou.dc.module.ratelimiter.model.Rule;
import lombok.Data;

/**
 * 限流器
 *
 * @Author: ZhouZhengAi
 * @Date: 2022/4/2 17:14
 */
@Data
public class TimeLimitHandleHolder {
//    private Class clazz;
    //最近一次时间窗口开启时间
    private Long timeWindowStartMillis;
    //规定时间内已请求次数
    private AtomicLong requestCount;
//    // 限流间隔毫秒数
//    private long limitMill;
//    //限制请求最大值
//    private long limitCount;

//规则信息
    private Rule rule;

}
