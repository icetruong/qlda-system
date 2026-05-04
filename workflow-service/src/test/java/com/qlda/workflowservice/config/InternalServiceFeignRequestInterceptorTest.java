package com.qlda.workflowservice.config;

import feign.RequestTemplate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InternalServiceFeignRequestInterceptorTest {

    @Test
    void shouldAttachInternalHeaders() {
        InternalAuthProperties properties = new InternalAuthProperties();
        properties.setServiceName("workflow-service");
        properties.setServiceToken("secret-token");
        properties.setApiKey("secret-api-key");
        properties.setAllowedServices(List.of("document-service"));

        InternalServiceFeignRequestInterceptor interceptor = new InternalServiceFeignRequestInterceptor(properties);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertEquals("Bearer secret-token", template.headers().get("Authorization").iterator().next());
        assertEquals("workflow-service", template.headers().get("X-Service-Name").iterator().next());
        assertEquals("secret-api-key", template.headers().get("INTERNAL_API_KEY").iterator().next());
    }
}
