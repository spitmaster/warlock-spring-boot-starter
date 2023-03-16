package io.github.spitmaster.warlock.aspect.semaphore;

import io.github.spitmaster.warlock.annotation.Warlock;
import io.github.spitmaster.warlock.core.factory.semaphore.WmutexFactory;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * 专门处理 Wsemaphore 注解的advisor
 */
public class WsemaphoreAnnotationAdvisor extends AbstractPointcutAdvisor implements BeanFactoryAware, InitializingBean {
    private BeanFactory beanFactory;
    private Pointcut pointcut;
    private Advice advice;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.pointcut == null) {
            this.pointcut = new AnnotationMatchingPointcut(null, Warlock.class, true);
        }
        if (this.advice == null) {
            this.advice = new WsemaphoreMethodInterceptor(beanFactory.getBean(WmutexFactory.class));
        }
    }
}
