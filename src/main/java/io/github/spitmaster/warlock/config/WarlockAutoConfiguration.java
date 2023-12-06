package io.github.spitmaster.warlock.config;

import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.annotation.WcyclicBarrier;
import io.github.spitmaster.warlock.annotation.WrateLimiter;
import io.github.spitmaster.warlock.annotation.Wsemaphore;
import io.github.spitmaster.warlock.aspect.WaroundMethodInterceptor;
import io.github.spitmaster.warlock.core.factory.TimeoutHandlerProvider;
import io.github.spitmaster.warlock.core.factory.WaroundFactory;
import io.github.spitmaster.warlock.core.factory.barrier.DefaultWbarrierFactory;
import io.github.spitmaster.warlock.core.factory.lock.DefaultWlockFactory;
import io.github.spitmaster.warlock.core.factory.ratelimiter.DefaultWlimiterFactory;
import io.github.spitmaster.warlock.core.factory.semaphore.DefaultWmutexFactory;
import org.aopalliance.aop.Advice;
import org.redisson.api.RedissonClient;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
    @ConditionalOnBean(RedissonClient.class)
    public AbstractPointcutAdvisor warlockAdvisorWithRedisson(
            RedissonClient redissonClient,
            TimeoutHandlerProvider timeoutHandlerProvider) {
        return warlockAdvisor(redissonClient, timeoutHandlerProvider);
    }

    @Bean("warlockAnnotationAdvisor")
    @ConditionalOnMissingBean(RedissonClient.class)
    public AbstractPointcutAdvisor warlockAdvisorWithoutRedisson(
            TimeoutHandlerProvider timeoutHandlerProvider) {
        return warlockAdvisor(null, timeoutHandlerProvider);
    }

    private static AbstractPointcutAdvisor warlockAdvisor(RedissonClient redissonClient, TimeoutHandlerProvider timeoutHandlerProvider) {
        AnnotationMatchingPointcut warlockPointcut = new AnnotationMatchingPointcut(null, Warlock.class, true);
        WaroundFactory defaultWlockFactory = new DefaultWlockFactory(redissonClient, timeoutHandlerProvider);
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
    public AbstractPointcutAdvisor wcyclicBarrierAnnotationAdvisor(
            TimeoutHandlerProvider timeoutHandlerProvider) {
        AnnotationMatchingPointcut wcyclicBarrierPointcut = new AnnotationMatchingPointcut(null, WcyclicBarrier.class, true);
        DefaultWbarrierFactory defaultWbarrierFactory = new DefaultWbarrierFactory(timeoutHandlerProvider);
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

    /**
     * wsemaphore注解的切面
     */
    @Bean("wsemaphoreAdvisor")
    @ConditionalOnBean(RedissonClient.class)
    public AbstractPointcutAdvisor wsemaphoreAdvisorWithRedisson(
            @Autowired(required = false) RedissonClient redissonClient,
            TimeoutHandlerProvider timeoutHandlerProvider) {
        return wsemaphoreAdvisor(redissonClient, timeoutHandlerProvider);
    }

    @Bean("wsemaphoreAdvisor")
    @ConditionalOnMissingBean(RedissonClient.class)
    public AbstractPointcutAdvisor wsemaphoreAdvisorWithoutRedisson(
            TimeoutHandlerProvider timeoutHandlerProvider) {
        return wsemaphoreAdvisor(null, timeoutHandlerProvider);
    }

    private static AbstractPointcutAdvisor wsemaphoreAdvisor(RedissonClient redissonClient, TimeoutHandlerProvider timeoutHandlerProvider) {
        AnnotationMatchingPointcut wsemaphorePointcut = new AnnotationMatchingPointcut(null, Wsemaphore.class, true);
        DefaultWmutexFactory defaultWmutexFactory = new DefaultWmutexFactory(redissonClient, timeoutHandlerProvider);
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
    @Bean("wrateLimiterAdvisor")
    @ConditionalOnBean(RedissonClient.class)
    public AbstractPointcutAdvisor wrateLimiterAdvisorWithRedisson(
            @Autowired(required = false) RedissonClient redissonClient,
            TimeoutHandlerProvider timeoutHandlerProvider) {
        return wrateLimiterAdvisor(redissonClient, timeoutHandlerProvider);
    }

    @Bean("wrateLimiterAdvisor")
    @ConditionalOnMissingBean(RedissonClient.class)
    public AbstractPointcutAdvisor wrateLimiterAdvisorWithoutRedisson(
            TimeoutHandlerProvider timeoutHandlerProvider) {
        return wrateLimiterAdvisor(null, timeoutHandlerProvider);
    }

    private static AbstractPointcutAdvisor wrateLimiterAdvisor(RedissonClient redissonClient, TimeoutHandlerProvider timeoutHandlerProvider) {
        AnnotationMatchingPointcut wrateLimiterPointcut = new AnnotationMatchingPointcut(null, WrateLimiter.class, true);
        DefaultWlimiterFactory defaultWlimiterFactory = new DefaultWlimiterFactory(redissonClient, timeoutHandlerProvider);
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

    /**
     * 超时策略的handler
     */
    @Bean
    public TimeoutHandlerProvider timeoutHandlerProvider(BeanFactory beanFactory) {
        //这里不能直接将 WaitTimeoutHandler 和 LeaseTimeoutHandler 一把找出来
        //某些情况下, 会造成循环依赖, 导致切面失效
        //只能在运行时通过BeanFactory去找
        return new TimeoutHandlerProvider(beanFactory);
    }
}