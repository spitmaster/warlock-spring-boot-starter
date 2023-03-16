package io.github.spitmaster.warlock.config;

import io.github.spitmaster.warlock.aspect.WarlockAnnotationAdvisor;
import io.github.spitmaster.warlock.aspect.WcyclicBarrierAnnotationAdvisor;
import io.github.spitmaster.warlock.aspect.WrateLimiterAnnotationAdvisor;
import io.github.spitmaster.warlock.aspect.WsemaphoreAnnotationAdvisor;
import io.github.spitmaster.warlock.core.factory.barrier.DefaultWbarrierFactory;
import io.github.spitmaster.warlock.core.factory.lock.DefaultWlockFactory;
import io.github.spitmaster.warlock.core.factory.lock.DistributedWlockFactory;
import io.github.spitmaster.warlock.core.factory.lock.StandaloneWlockFactory;
import io.github.spitmaster.warlock.core.factory.ratelimiter.DefaultWlimiterFactory;
import io.github.spitmaster.warlock.core.factory.semaphore.DefaultWmutexFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * warlock的默认配置
 * 觉得不满足你的要求, 可以换成你自己实现的Bean
 *
 * @author zhouyijin
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "warlock.enabled", matchIfMissing = true)
public class WarlockAutoConfiguration {

    /**
     * @return 切面
     * @Warlock 注解的切面配置
     */
    @Bean("warlockAnnotationAdvisor")
    public WarlockAnnotationAdvisor warlockAnnotationAdvisor() {
        return new WarlockAnnotationAdvisor();
    }

    /**
     * 构造Warlock的工厂对象
     * 如果默认的Warlock工厂不能满足你的要求, 你可以自己重新实现
     *
     * @return 构造warlock的工厂
     */
    @Bean("warlockFactory")
    public DefaultWlockFactory warlockFactory() {
        return new DefaultWlockFactory();
    }

    @Bean("standaloneWlockFactory")
    public StandaloneWlockFactory standaloneWlockFactory() {
        //专门生成单机锁的工厂
        return new StandaloneWlockFactory();
    }

    @Bean("distributedWlockFactory")
    public DistributedWlockFactory distributedWlockFactory() {
        //专门生成分布式锁的工厂
        return new DistributedWlockFactory();
    }


    /*
    -------------------------------------------------------------------------------------
     */


    @Bean("wcyclicBarrierAnnotationAdvisor")
    public WcyclicBarrierAnnotationAdvisor wcyclicBarrierAnnotationAdvisor() {
        return new WcyclicBarrierAnnotationAdvisor();
    }

    @Bean("wbarrierFactory")
    public DefaultWbarrierFactory wbarrierFactory() {
        return new DefaultWbarrierFactory();
    }


    /*
    -------------------------------------------------------------------------------------
     */

    @Bean("wsemaphoreAnnotationAdvisor")
    public WsemaphoreAnnotationAdvisor wsemaphoreAnnotationAdvisor() {
        return new WsemaphoreAnnotationAdvisor();
    }

    @Bean("wmutexFactory")
    public DefaultWmutexFactory wmutexFactory() {
        return new DefaultWmutexFactory();
    }

    /*
    -------------------------------------------------------------------------------------
     */

    @Bean("wlimiterFactory")
    public DefaultWlimiterFactory wlimiterFactory() {
        return new DefaultWlimiterFactory();
    }

    @Bean("wrateLimiterAnnotationAdvisor")
    public WrateLimiterAnnotationAdvisor wrateLimiterAnnotationAdvisor() {
        return new WrateLimiterAnnotationAdvisor();
    }

}