package io.github.spitmaster.warlock.config;

import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.annotation.WcyclicBarrier;
import io.github.spitmaster.warlock.annotation.WrateLimiter;
import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.aspect.WaroundMethodInterceptor;
import io.github.spitmaster.warlock.core.factory.WaroundFactory;
import io.github.spitmaster.warlock.core.factory.barrier.DefaultWbarrierFactory;
import io.github.spitmaster.warlock.core.factory.lock.DefaultWlockFactory;
import io.github.spitmaster.warlock.core.factory.lock.DistributedWlockFactory;
import io.github.spitmaster.warlock.core.factory.lock.StandaloneWlockFactory;
import io.github.spitmaster.warlock.core.factory.ratelimiter.DefaultWlimiterFactory;
import io.github.spitmaster.warlock.core.factory.semaphore.DefaultWmutexFactory;
import org.aopalliance.aop.Advice;
import org.redisson.api.RedissonClient;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
     * Warlock注解的切面配置
     */
    @Bean("warlockAnnotationAdvisor")
    public AbstractPointcutAdvisor warlockAnnotationAdvisor(@Autowired BeanFactory beanFactory, @Autowired(required = false) RedissonClient redissonClient) {
        AnnotationMatchingPointcut warlockPointcut = new AnnotationMatchingPointcut(null, Warlock.class, true);
        WaroundFactory defaultWlockFactory = new DefaultWlockFactory(
                new StandaloneWlockFactory(beanFactory), //单机锁工厂
                new DistributedWlockFactory(beanFactory, redissonClient) //分布式锁工厂
        );
        WaroundMethodInterceptor waroundMethodInterceptor = new WaroundMethodInterceptor(defaultWlockFactory);
        return new AbstractPointcutAdvisor() {
            @Override
            public Pointcut getPointcut() {
                return warlockPointcut;
            }

            @Override
            public Advice getAdvice() {
                return waroundMethodInterceptor;
            }
        };
    }

    /**
     * WcyclicBarrier注解的切面
     */
    @Bean("wcyclicBarrierAnnotationAdvisor")
    public AbstractPointcutAdvisor wcyclicBarrierAnnotationAdvisor(@Autowired BeanFactory beanFactory) {
        AnnotationMatchingPointcut wcyclicBarrierPointcut = new AnnotationMatchingPointcut(null, WcyclicBarrier.class, true);
        DefaultWbarrierFactory defaultWbarrierFactory = new DefaultWbarrierFactory(beanFactory);
        WaroundMethodInterceptor waroundMethodInterceptor = new WaroundMethodInterceptor(defaultWbarrierFactory);
        return new AbstractPointcutAdvisor() {
            @Override
            public Pointcut getPointcut() {
                return wcyclicBarrierPointcut;
            }

            @Override
            public Advice getAdvice() {
                return waroundMethodInterceptor;
            }
        };
    }


    //-------------------------------------------------------------------------------------


    /**
     * wsemaphore注解的切面
     */
    @Bean("wsemaphoreAnnotationAdvisor")
    public AbstractPointcutAdvisor wsemaphoreAnnotationAdvisor(@Autowired BeanFactory beanFactory, @Autowired(required = false) RedissonClient redissonClient) {
        AnnotationMatchingPointcut wsemaphorePointcut = new AnnotationMatchingPointcut(null, Wsemaphore.class, true);
        DefaultWmutexFactory defaultWmutexFactory = new DefaultWmutexFactory(beanFactory, redissonClient);
        WaroundMethodInterceptor wsemaphoreMethodInterceptor = new WaroundMethodInterceptor(defaultWmutexFactory);
        return new AbstractPointcutAdvisor() {
            @Override
            public Pointcut getPointcut() {
                return wsemaphorePointcut;
            }

            @Override
            public Advice getAdvice() {
                return wsemaphoreMethodInterceptor;
            }
        };
    }

    /**
     * wrateLimiter注解的切面
     */
    @Bean("wrateLimiterAnnotationAdvisor")
    public AbstractPointcutAdvisor wrateLimiterAnnotationAdvisor(@Autowired BeanFactory beanFactory, @Autowired(required = false) RedissonClient redissonClient) {
        AnnotationMatchingPointcut wrateLimiterPointcut = new AnnotationMatchingPointcut(null, WrateLimiter.class, true);
        DefaultWlimiterFactory defaultWlimiterFactory = new DefaultWlimiterFactory(beanFactory, redissonClient);
        WaroundMethodInterceptor wrateLimiterMethodInterceptor = new WaroundMethodInterceptor(defaultWlimiterFactory);
        return new AbstractPointcutAdvisor() {
            @Override
            public Pointcut getPointcut() {
                return wrateLimiterPointcut;
            }

            @Override
            public Advice getAdvice() {
                return wrateLimiterMethodInterceptor;
            }
        };
    }
}