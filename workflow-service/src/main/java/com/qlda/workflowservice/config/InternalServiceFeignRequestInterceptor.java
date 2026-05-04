package com.qlda.workflowservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class InternalServiceFeignRequestInterceptor implements RequestInterceptor {
    public static final String AUTHORIZATION = "Authorization";
    public static final String SERVICE_NAME_HEADER = "X-Service-Name";
    public static final String INTERNAL_API_KEY_HEADER = "INTERNAL_API_KEY";

    private final InternalAuthProperties properties;

    @Override
    public void apply(RequestTemplate template) {
        template.header(AUTHORIZATION, "Bearer " + properties.getServiceToken());
        template.header(SERVICE_NAME_HEADER, properties.getServiceName());
        if (StringUtils.hasText(properties.getApiKey())) {
            template.header(INTERNAL_API_KEY_HEADER, properties.getApiKey());
        }
    }
}
