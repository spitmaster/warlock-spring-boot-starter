package com.zyj.warlock.config;

import com.zyj.warlock.aspect.WarlockAspect;
import com.zyj.warlock.core.factory.DefaultWlockFactory;
import com.zyj.warlock.core.factory.WlockFactory;
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
    @Bean
    @ConditionalOnMissingBean
    public WlockFactory warlockFactory() {
        return new DefaultWlockFactory();
    }

}