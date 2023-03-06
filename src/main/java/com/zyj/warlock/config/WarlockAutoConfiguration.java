package com.zyj.warlock.config;

import com.zyj.warlock.aspect.WarlockAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "warlock.enabled", matchIfMissing = true)
//@EnableConfigurationProperties(WarlockProperties.class)
public class WarlockAutoConfiguration {

    /**
     * Warlock 注解的切面
     *
     * @return 切面
     */
    @Bean
    @ConditionalOnMissingBean
    public WarlockAspect warlockAspect() {
        return new WarlockAspect();
    }
}