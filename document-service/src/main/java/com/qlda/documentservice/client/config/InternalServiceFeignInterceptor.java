package com.qlda.documentservice.client.config;

import com.qlda.documentservice.config.InternalAuthProperties;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

@Component
public class InternalServiceFeignInterceptor implements RequestInterceptor {

    private final InternalAuthProperties internalAuthProperties;

    public InternalServiceFeignInterceptor(InternalAuthProperties internalAuthProperties) {
        this.internalAuthProperties = internalAuthProperties;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", "Bearer " + internalAuthProperties.getServiceToken());
        template.header("X-Service-Name", internalAuthProperties.getServiceName());
    }
}
