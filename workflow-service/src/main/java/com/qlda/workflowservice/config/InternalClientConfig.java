package com.qlda.workflowservice.config;

import feign.RequestInterceptor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(InternalAuthProperties.class)
public class InternalClientConfig {

    @Bean
    public RequestInterceptor internalServiceRequestInterceptor(InternalAuthProperties properties) {
        return new InternalServiceFeignRequestInterceptor(properties);
    }
}
