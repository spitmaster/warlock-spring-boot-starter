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
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 在没有Redisson的情况下的配置
 * warlock的默认配置
 *
 * @author zhouyijin
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(type = "org.redisson.api.RedissonClient")
@ConditionalOnProperty(name = "warlock.enabled", matchIfMissing = true)
public class WarlockWithoutRedissonAutoConfiguration {

    /**
     * Warlock注解的切面配置
     */
    @Bean("warlockAnnotationAdvisor")
    public AbstractPointcutAdvisor warlockAdvisorWithoutRedisson(
            TimeoutHandlerProvider timeoutHandlerProvider) {
        AnnotationMatchingPointcut warlockPointcut = new AnnotationMatchingPointcut(null, Warlock.class, true);
        WaroundFactory defaultWlockFactory = new DefaultWlockFactory(null, timeoutHandlerProvider);
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
    public AbstractPointcutAdvisor wsemaphoreAdvisorWithoutRedisson(
            TimeoutHandlerProvider timeoutHandlerProvider) {
        AnnotationMatchingPointcut wsemaphorePointcut = new AnnotationMatchingPointcut(null, Wsemaphore.class, true);
        DefaultWmutexFactory defaultWmutexFactory = new DefaultWmutexFactory(null, timeoutHandlerProvider);
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
    public AbstractPointcutAdvisor wrateLimiterAdvisorWithoutRedisson(
            TimeoutHandlerProvider timeoutHandlerProvider) {
        AnnotationMatchingPointcut wrateLimiterPointcut = new AnnotationMatchingPointcut(null, WrateLimiter.class, true);
        DefaultWlimiterFactory defaultWlimiterFactory = new DefaultWlimiterFactory(null, timeoutHandlerProvider);
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