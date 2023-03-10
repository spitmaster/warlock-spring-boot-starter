package io.github.spitmaster.warlock.config;

import io.github.spitmaster.warlock.aspect.WcyclicBarrierAspect;
import io.github.spitmaster.warlock.core.factory.barrier.DefaultWbarrierFactory;
import io.github.spitmaster.warlock.core.factory.barrier.WbarrierFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WcyclicBarrier 的默认配置
 *
 * @author zhouyijin
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "warlock.enabled", matchIfMissing = true)
public class WcyclicBarrierAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WcyclicBarrierAspect wsemaphoreAspect(WbarrierFactory wbarrierFactory) {
        return new WcyclicBarrierAspect(wbarrierFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public WbarrierFactory wbarrierFactory(BeanFactory beanFactory) {
        return new DefaultWbarrierFactory(beanFactory);
    }
}