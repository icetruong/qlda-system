package com.qlda.workflowservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "internal.auth")
public class InternalAuthProperties {
    private String serviceName;
    private String serviceToken;
    private String apiKey;
    private List<String> allowedServices = new ArrayList<>();
}
