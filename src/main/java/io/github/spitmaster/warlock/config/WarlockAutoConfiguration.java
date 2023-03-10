package io.github.spitmaster.warlock.config;

import io.github.spitmaster.warlock.aspect.WarlockAspect;
import io.github.spitmaster.warlock.core.factory.lock.DefaultWlockFactory;
import io.github.spitmaster.warlock.core.factory.lock.DistributedWlockFactory;
import io.github.spitmaster.warlock.core.factory.lock.StandaloneWlockFactory;
import io.github.spitmaster.warlock.core.factory.lock.WlockFactory;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
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
     * Warlock 注解的切面
     *
     * @return 切面
     */
    @Bean
    @ConditionalOnMissingBean
    public WarlockAspect warlockAspect(WlockFactory wlockFactory) {
        return new WarlockAspect(wlockFactory);
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
    public WlockFactory warlockFactory(
            @Qualifier("standaloneWlockFactory") @Autowired(required = true) StandaloneWlockFactory standaloneWlockFactory,
            @Qualifier("distributedWlockFactory") @Autowired(required = false) DistributedWlockFactory distributedWlockFactory

    ) {
        return new DefaultWlockFactory(standaloneWlockFactory, distributedWlockFactory);
    }

    @Bean("standaloneWlockFactory")
    @ConditionalOnMissingBean
    public StandaloneWlockFactory standaloneWlockFactory(BeanFactory beanFactory) {
        //专门生成单机锁的工厂
        return new StandaloneWlockFactory(beanFactory);
    }

    @Bean("distributedWlockFactory")
    @ConditionalOnBean(RedissonClient.class)
    @ConditionalOnMissingBean
    public DistributedWlockFactory distributedWlockFactory(
            BeanFactory beanFactory,
            @Qualifier("distributedWlockFactory") @Autowired(required = false) RedissonClient redissonClient) {
        //专门生成分布式锁的工厂
        return new DistributedWlockFactory(beanFactory, redissonClient);
    }
}