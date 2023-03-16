package io.github.spitmaster.warlock.config;

import io.github.spitmaster.warlock.aspect.barrier.WcyclicBarrierAnnotationAdvisor;
import io.github.spitmaster.warlock.aspect.barrier.WcyclicBarrierMethodInterceptor;
import io.github.spitmaster.warlock.aspect.ratelimiter.WrateLimiterAnnotationAdvisor;
import io.github.spitmaster.warlock.aspect.semaphore.WsemaphoreAnnotationAdvisor;
import io.github.spitmaster.warlock.aspect.warlock.WarlockAnnotationAdvisor;
import io.github.spitmaster.warlock.core.factory.barrier.DefaultWbarrierFactory;
import io.github.spitmaster.warlock.core.factory.barrier.WbarrierFactory;
import io.github.spitmaster.warlock.core.factory.lock.DefaultWlockFactory;
import io.github.spitmaster.warlock.core.factory.lock.DistributedWlockFactory;
import io.github.spitmaster.warlock.core.factory.lock.StandaloneWlockFactory;
import io.github.spitmaster.warlock.core.factory.lock.WlockFactory;
import io.github.spitmaster.warlock.core.factory.ratelimiter.DefaultWlimiterFactory;
import io.github.spitmaster.warlock.core.factory.ratelimiter.WlimiterFactory;
import io.github.spitmaster.warlock.core.factory.semaphore.DefaultWmutexFactory;
import io.github.spitmaster.warlock.core.factory.semaphore.WmutexFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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
     * @Warlock 注解的切面配置
     *
     * @return 切面
     */
    @Bean("warlockAnnotationAdvisor")
    @ConditionalOnMissingBean
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
    @Primary
    @ConditionalOnMissingBean
    public WlockFactory warlockFactory() {
        return new DefaultWlockFactory();
    }

    @Bean("standaloneWlockFactory")
    @ConditionalOnMissingBean
    public StandaloneWlockFactory standaloneWlockFactory() {
        //专门生成单机锁的工厂
        return new StandaloneWlockFactory();
    }

    @Bean("distributedWlockFactory")
    @ConditionalOnMissingBean
    public DistributedWlockFactory distributedWlockFactory() {
        //专门生成分布式锁的工厂
        return new DistributedWlockFactory();
    }


    /*
    -------------------------------------------------------------------------------------
     */


    @Bean("wcyclicBarrierAnnotationAdvisor")
    @ConditionalOnMissingBean
    public WcyclicBarrierAnnotationAdvisor wcyclicBarrierAnnotationAdvisor(){
        return new WcyclicBarrierAnnotationAdvisor();
    }

    @Bean("wbarrierFactory")
    @ConditionalOnMissingBean
    public WbarrierFactory wbarrierFactory(){
        return new DefaultWbarrierFactory();
    }


    /*
    -------------------------------------------------------------------------------------
     */

    @Bean("wsemaphoreAnnotationAdvisor")
    @ConditionalOnMissingBean
    public WsemaphoreAnnotationAdvisor wsemaphoreAnnotationAdvisor(){
        return new WsemaphoreAnnotationAdvisor();
    }

    @Bean("wmutexFactory")
    @ConditionalOnMissingBean
    public WmutexFactory wmutexFactory(){
        return new DefaultWmutexFactory();
    }

    /*
    -------------------------------------------------------------------------------------
     */

    @Bean("wlimiterFactory")
    @ConditionalOnMissingBean
    public WlimiterFactory wlimiterFactory(){
        return new DefaultWlimiterFactory();
    }

    @Bean("wrateLimiterAnnotationAdvisor")
    @ConditionalOnMissingBean
    public WrateLimiterAnnotationAdvisor wrateLimiterAnnotationAdvisor(){
        return new WrateLimiterAnnotationAdvisor();
    }

}